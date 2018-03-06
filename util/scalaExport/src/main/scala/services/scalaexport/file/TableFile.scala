package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.config.{ExportEnum, ExportModel}

object TableFile {
  def export(model: ExportModel, enums: Seq[ExportEnum]) = {
    val file = ScalaFile(model.tablePackage, model.className + "Table")

    file.addImport("services.database.SlickQueryService.imports", "_")

    model.fields.foreach(_.enumOpt.foreach(e => file.addImport(s"${e.tablePackage.mkString(".")}.${e.className}ColumnType", s"${e.propertyName}ColumnType")))

    file.add(s"object ${model.className}Table {", 1)
    file.add(s"val query = TableQuery[${model.className}Table]")
    addQueries(file, model)
    addReferences(file, model)
    file.add("}", -1)
    file.add()

    file.add(s"""class ${model.className}Table(tag: Tag) extends Table[${model.modelClass}](tag, "${model.tableName}") {""", 1)

    addFields(model, file, enums)
    file.add()

    if (model.pkFields.nonEmpty) {
      val pkProps = model.pkFields match {
        case h :: Nil => h.propertyName
        case x => "(" + x.map(_.propertyName).mkString(", ") + ")"
      }
      file.add(s"""val pk = primaryKey("pk_${model.tableName}", $pkProps)""")
      file.add()
    }
    if (model.fields.lengthCompare(22) > 0) {
      file.addImport("shapeless", "HNil")
      file.addImport("shapeless", "Generic")
      file.addImport("slickless", "_")

      val fieldStr = model.fields.map(_.propertyName).mkString(" :: ")
      file.add(s"override val * = ($fieldStr :: HNil).mappedWith(Generic[models.${model.fullClassName}])")
    } else {
      val propSeq = model.fields.map(_.propertyName).mkString(", ")
      file.add(s"override val * = ($propSeq) <> (", 1)
      file.add(s"(${model.modelClass}.apply _).tupled,")
      file.add(s"${model.modelClass}.unapply")
      file.add(")", -1)
    }

    file.add("}", -1)
    file.add()
    file

  }

  private[this] def addFields(model: ExportModel, file: ScalaFile, enums: Seq[ExportEnum]) = model.fields.foreach { field =>
    field.addImport(file)
    val colScala = field.t match {
      case ColumnType.ArrayType => ColumnType.ArrayType.valForSqlType(field.sqlTypeName)
      case _ => field.scalaType
    }
    val propType = if (field.notNull) { colScala } else { "Option[" + colScala + "]" }
    field.description.foreach(d => file.add("/** " + d + " */"))
    file.add(s"""val ${field.propertyName} = column[$propType]("${field.columnName}")""")
  }

  private[this] def addQueries(file: ScalaFile, model: ExportModel) = {
    model.pkFields.foreach(_.addImport(file))
    model.pkFields match {
      case Nil => // noop
      case field :: Nil =>
        file.add()
        val colProp = field.propertyName
        file.add(s"def getByPrimaryKey($colProp: ${field.scalaType}) = query.filter(_.$colProp === $colProp).result.headOption")
        val seqArgs = s"${colProp}Seq: Seq[${field.scalaType}]"
        file.add(s"def getByPrimaryKeySeq($seqArgs) = query.filter(_.$colProp.inSet(${colProp}Seq)).result")
      case fields => // multiple columns
        file.add()
        val colArgs = fields.map(f => f.propertyName + ": " + f.scalaType).mkString(", ")
        val queryArgs = fields.map(f => "o." + f.propertyName + " === " + f.propertyName).mkString(" && ")
        file.add(s"def getByPrimaryKey($colArgs) = query.filter(o => $queryArgs).result.headOption")
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
          file.add(s"""def getBy$propCls($propId: ${col.scalaType}) = query.filter(_.$propId === $propId).result""")
          file.add(s"""def getBy${propCls}Seq(${propId}Seq: Seq[${col.scalaType}]) = query.filter(_.$propId.inSet(${propId}Seq)).result""")
        case _ => // noop
      }
    }
  }
}
