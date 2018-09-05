package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.MarkdownFile

object WikiListFiles {
  def export(config: ExportConfiguration, models: Seq[ExportModel]) = {
    val file = MarkdownFile(Seq("database"), "Database")
    file.addHeader("Database")

    file.addHeader(s"[Tables](DatabaseTables)", 2)
    models.foreach { m =>
      file.add(s" - [${m.tableName}](DatabaseTable${m.className})")
    }
    file.add()

    file.addHeader(s"[Enums](DatabaseEnums)", 2)
    config.enums.foreach { e =>
      file.add(s" - [${e.name}](DatabaseEnum${e.className})")
    }

    val tableFile = MarkdownFile(Seq("database"), "DatabaseTables")
    tableFile.addHeader("Database Tables")
    models.foreach { m =>
      tableFile.add(s" - [${m.tableName}](DatabaseTable${m.className})")
    }

    val enumFiles = if (config.enums.isEmpty) {
      Nil
    } else {
      val enumFile = MarkdownFile(Seq("database"), "DatabaseEnums")
      enumFile.addHeader("Database Enums")
      config.enums.foreach { e =>
        enumFile.add(s" - [${e.name}](DatabaseEnum${e.className})")
      }
      Seq(enumFile)
    }

    Seq(file, tableFile) ++ enumFiles
  }
}
