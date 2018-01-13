package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportEnum

object EnumColumnTypeFile {
  def export(enum: ExportEnum) = {
    val file = ScalaFile(enum.tablePackage, enum.className + "EnumColumnType", None)

    file.addImport(enum.modelPackage.mkString("."), enum.className)
    file.addImport("services.database.SlickQueryService.imports", "_")

    file.add(s"object ${enum.className}EnumColumnType {", 1)
    file.add(s"implicit val columnType = MappedColumnType.base[${enum.className}, String](_.value, ${enum.className}.withValue)")
    file.add("}", -1)

    file
  }
}
