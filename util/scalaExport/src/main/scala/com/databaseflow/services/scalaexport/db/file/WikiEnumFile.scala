package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportEnum
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.MarkdownFile

object WikiEnumFile {
  def export(config: ExportConfiguration, e: ExportEnum) = {
    val file = MarkdownFile("database" +: e.pkg, "DatabaseEnum" + e.className)
    file.addHeader(e.name)
    e.values.foreach(v => file.add(s" - $v"))
    file
  }
}
