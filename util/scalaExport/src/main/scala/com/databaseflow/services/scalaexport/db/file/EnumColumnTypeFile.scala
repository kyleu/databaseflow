package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportEnum
import com.databaseflow.models.scalaexport.file.ScalaFile

object EnumColumnTypeFile {
  def export(providedPrefix: String, enum: ExportEnum) = {
    val file = ScalaFile(enum.tablePackage, enum.className + "ColumnType", None)

    file.addImport(enum.modelPackage.mkString("."), enum.className)
    file.addImport(providedPrefix + "services.database.SlickQueryService.imports", "_")
    file.addImport("slick.jdbc", "JdbcType")

    file.add(s"object ${enum.className}ColumnType {", 1)
    file.add(s"implicit val ${enum.propertyName}ColumnType: JdbcType[${enum.className}] = MappedColumnType.base[${enum.className}, String](_.value, ${enum.className}.withValue)")
    file.add("}", -1)

    file
  }
}
