package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.{MarkdownFile, MarkdownHelper}

object WikiTableFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = MarkdownFile("database" +: model.pkg, "DatabaseTable" + model.className)
    file.addHeader(model.tableName)

    file.addHeader("Columns", 2)
    MarkdownHelper.table(file, Seq(
      ('l', 30, "Name"), ('l', 20, "Type"), ('l', 8, "NotNull"), ('l', 8, "Unique"), ('l', 10, "Indexed"), ('l', 20, "Default")
    ), model.fields.map { f =>
      Seq(f.columnName, f.t.toString, f.notNull.toString, f.unique.toString, f.indexed.toString, f.defaultValue.getOrElse(""))
    })
    file.add()

    if (model.references.nonEmpty) {
      file.addHeader("References", 2)
      MarkdownHelper.table(file, Seq(('l', 30, "Name"), ('l', 20, "Target"), ('l', 40, "Table"), ('l', 20, "Column")), model.references.map { r =>
        val src = config.getModel(r.srcTable)
        Seq(r.name, r.tgt, MarkdownHelper.link(r.srcTable, s"DatabaseTable${src.className}"), r.srcCol)
      })
    }

    file
  }
}
