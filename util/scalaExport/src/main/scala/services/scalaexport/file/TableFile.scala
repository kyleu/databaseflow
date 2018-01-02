package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.config.ExportModel

object TableFile {
  def export(model: ExportModel) = {
    val file = ScalaFile(model.tablePackage, model.className + "Table")

    file.addImport("services.database.SlickQueryService.imports", "_")

    file.add(s"object ${model.className}Table {", 1)
    file.add(s"val query = TableQuery[${model.className}Table]")
    addQueries(file, model)
    addReferences(file, model)
    file.add("}", -1)
    file.add()

    file.add(s"""class ${model.className}Table(tag: Tag) extends Table[${model.modelClass}](tag, "${model.tableName}") {""", 1)

    addFields(model, file)
    file.add()

    if (model.pkFields.nonEmpty) {
      val pkProps = model.pkFields match {
        case h :: Nil => h.propertyName
        case x => "(" + x.map(_.propertyName).mkString(", ") + ")"
      }
      file.add(s"""val pk = primaryKey("pk_${model.tableName}", $pkProps)""")
      file.add()
    }

    val propSeq = model.fields.map(_.propertyName).mkString(", ")
    file.add(s"override val * = ($propSeq) <> (", 1)
    file.add(s"(${model.modelClass}.apply _).tupled,")
    file.add(s"${model.modelClass}.unapply")
    file.add(")", -1)

    file.add("}", -1)
    file.add()
    file
  }

  private[this] def addFields(model: ExportModel, file: ScalaFile) = model.fields.foreach { field =>
    field.t.requiredImport.foreach(p => file.addImport(p, field.t.asScala))

    val colScala = field.t match {
      case ColumnType.ArrayType => ColumnType.ArrayType.valForSqlType(field.sqlTypeName)
      case x => x.asScala
    }
    val propType = if (field.notNull) { colScala } else { "Option[" + colScala + "]" }
    field.description.foreach(d => file.add("/** " + d + " */"))
    file.add(s"""val ${field.propertyName} = column[$propType]("${field.columnName}")""")
  }

  private[this] def addQueries(file: ScalaFile, model: ExportModel) = {
    model.pkFields.foreach(field => field.t.requiredImport.foreach(pkg => file.addImport(pkg, field.t.asScala)))
    model.pkFields match {
      case Nil => // noop
      case field :: Nil =>
        file.add()
        val colProp = field.propertyName
        file.add(s"def getByPrimaryKey($colProp: ${field.t.asScala}) = query.filter(_.$colProp === $colProp).result.headOption")
        val seqArgs = s"${colProp}Seq: Seq[${field.t.asScala}]"
        file.add(s"def getByPrimaryKeySeq($seqArgs) = query.filter(_.$colProp.inSet(${colProp}Seq)).result")
      case fields => // multiple columns
        file.add()
        val colArgs = fields.map(f => f.propertyName + ": " + f.t.asScala).mkString(", ")
        val queryArgs = fields.map(f => "o." + f.propertyName + " === " + f.propertyName).mkString(" && ")
        file.add(s"def getByPrimaryKey($colArgs) = query.filter(o => $queryArgs).result.headOption")
    }
  }

  private[this] def addReferences(file: ScalaFile, model: ExportModel) = if (model.foreignKeys.nonEmpty) {
    model.foreignKeys.foreach { fk =>
      fk.references.toList match {
        case h :: Nil =>
          val col = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
          col.t.requiredImport.foreach(pkg => file.addImport(pkg, col.t.asScala))
          val propId = col.propertyName
          val propCls = col.className

          file.add()
          file.add(s"""def getBy$propCls($propId: ${col.t.asScala}) = query.filter(_.$propId === $propId).result""")
          file.add(s"""def getBy${propCls}Seq(${propId}Seq: Seq[${col.t.asScala}]) = query.filter(_.$propId.inSet(${propId}Seq)).result""")
        case _ => // noop
      }
    }
  }
}
