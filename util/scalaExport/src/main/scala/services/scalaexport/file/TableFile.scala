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
    file.add("}", -1)
    file.add()

    file.add(s"""class ${model.className}Table(tag: Tag) extends Table[${model.modelClass}](tag, "${model.tableName}") {""", 1)

    addFields(model, file)

    file.add()
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
    val attrs = if (model.pkFields.contains(field)) { ", O.PrimaryKey" } else { "" }
    file.add(s"""val ${field.propertyName} = column[$propType]("${field.columnName}"$attrs)""")
  }
}
