package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.ScalaFile

object DoobieFile {
  private[this] val tq = "\"\"\""

  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(pkg = model.doobiePackage, key = model.className + "Doobie", core = true)
    val cols = model.fields.map(_.columnName)
    val quotedCols = cols.map("\"" + _ + "\"").mkString(", ")

    file.addImport("cats.data", "NonEmptyList")
    file.addImport(model.modelPackage.mkString("."), model.className)
    if (model.pkg.nonEmpty) {
      file.addImport(config.providedPrefix + "services.database.doobie", "DoobieQueries")
    }
    file.addImport(config.providedPrefix + "services.database.doobie.DoobieQueryService.Imports", "_")

    model.fields.foreach(_.enumOpt.foreach { e =>
      file.addImport(s"${e.doobiePackage.mkString(".")}.${e.className}Doobie", s"${e.propertyName}Meta")
    })

    file.add(s"""object ${model.className}Doobie extends DoobieQueries[${model.className}]("${model.tableName}") {""", 1)

    file.add(s"""override val countFragment = fr${tq}select count(*) from "${model.tableName}"$tq""")
    file.add(s"""override val selectFragment = fr${tq}select $quotedCols from "${model.tableName}"$tq""")
    file.add()

    file.add(s"""override val columns = Seq(${cols.map("\"" + _ + "\"").mkString(", ")})""")
    file.add(s"override val searchColumns = Seq(${model.searchFields.map("\"" + _.columnName + "\"").mkString(", ")})")
    file.add()

    file.add("override def searchFragment(q: String) = {", 1)
    file.add(s"""fr$tq${cols.map("\"" + _ + "\"::text = $q").mkString(" or ")}$tq""")
    file.add("}", -1)

    addQueries(file, model)
    addReferences(file, model)

    file.add("}", -1)
    file.add()
    file
  }

  private[this] def addQueries(file: ScalaFile, model: ExportModel) = {
    model.pkFields.foreach(_.addImport(file))
    model.pkFields match {
      case Nil => // noop
      case field :: Nil =>
        file.add()
        val colProp = field.propertyName

        val sql = s"""(selectFragment ++ whereAnd(fr"$colProp = $$$colProp"))"""
        file.add(s"def getByPrimaryKey($colProp: ${field.scalaType}) = $sql.query[Option[${model.className}]].unique")

        val seqArgs = s"${colProp}Seq: NonEmptyList[${field.scalaType}]"
        file.add(s"""def getByPrimaryKeySeq($seqArgs) = (selectFragment ++ in(fr"$colProp", ${colProp}Seq)).query[${model.className}].to[Seq]""")
      case fields => // multiple columns
        file.add()
        val colArgs = fields.map(f => f.propertyName + ": " + f.scalaType).mkString(", ")
        val queryArgs = fields.map(f => "o." + f.propertyName + " === " + f.propertyName).mkString(" && ")
        file.add(s"// def getByPrimaryKey($colArgs) = ???")
    }
  }

  private[this] def addReferences(file: ScalaFile, model: ExportModel) = if (model.foreignKeys.nonEmpty) {
    model.foreignKeys.foreach { fk =>
      fk.references match {
        case h :: Nil =>
          val col = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
          col.addImport(file)
          val propId = col.propertyName
          val propCls = col.className

          file.add()
          file.add(s"""def countBy$propCls($propId: ${col.scalaType}) = (countFragment ++ whereAnd(fr"$propId = $$$propId")).query[Int].unique""")
          val sql = s"""(selectFragment ++ whereAnd(fr"$propId = $$$propId"))"""
          file.add(s"def getBy$propCls($propId: ${col.scalaType}) = $sql.query[${model.className}].to[Seq]")
          val seqSql = s"""(selectFragment ++ whereAnd(in(fr"$propId", ${propId}Seq)))"""
          file.add(s"def getBy${propCls}Seq(${propId}Seq: NonEmptyList[${col.scalaType}]) = $seqSql.query[${model.className}].to[Seq]")
        case _ => // noop
      }
    }
  }
}
