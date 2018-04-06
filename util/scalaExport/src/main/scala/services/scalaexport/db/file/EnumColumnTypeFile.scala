package services.scalaexport.db.file

import models.scalaexport.db.ExportEnum
import models.scalaexport.file.ScalaFile

object EnumColumnTypeFile {
  def export(enum: ExportEnum) = {
    val file = ScalaFile(enum.tablePackage, enum.className + "ColumnType", None)

    file.addImport(enum.modelPackage.mkString("."), enum.className)
    file.addImport("services.database.SlickQueryService.imports", "_")
    file.addImport("slick.jdbc", "JdbcType")

    file.add(s"object ${enum.className}ColumnType {", 1)
    file.add(s"implicit val ${enum.propertyName}ColumnType: JdbcType[${enum.className}] = MappedColumnType.base[${enum.className}, String](_.value, ${enum.className}.withValue)")
    file.add("}", -1)

    file
  }
}
