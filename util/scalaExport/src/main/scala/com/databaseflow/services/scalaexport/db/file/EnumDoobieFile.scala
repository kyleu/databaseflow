package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportEnum
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.ScalaFile

object EnumDoobieFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val file = ScalaFile(pkg = enum.doobiePackage, key = enum.className + "Doobie", root = None, core = true)

    file.addImport(enum.modelPackage.mkString("."), enum.className)
    file.addImport(config.providedPrefix + "services.database.DoobieQueryService.Imports", "_")

    file.add(s"object ${enum.className}Doobie {", 1)
    val cn = enum.className
    file.add(s"""implicit val ${enum.propertyName}Meta: Meta[$cn] = pgEnumStringOpt("$cn", $cn.withValueOpt, _.value)""")
    file.add("}", -1)

    file
  }
}
