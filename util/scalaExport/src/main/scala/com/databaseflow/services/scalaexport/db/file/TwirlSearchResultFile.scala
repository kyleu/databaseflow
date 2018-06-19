package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.TwirlFile

object TwirlSearchResultFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(model.viewPackage, model.propertyName + "SearchResult")

    file.add(s"""@(model: ${model.modelClass}, hit: String)<div class="search-result">""", 1)
    file.add(s"""<div class="right">${model.title}</div>""")
    file.add("<div>", 1)
    file.add(model.iconHtml(config.providedPrefix))
    if (model.pkFields.isEmpty) {
      file.add("@model")
    } else {
      val cs = model.pkFields.map(f => "model." + f.propertyName)
      file.add(s"""<a class="theme-text" href="@${model.routesClass}.view(${cs.mkString(", ")})">${cs.map("@" + _).mkString(", ")}</a>""")
    }
    file.add("</div>", -1)
    file.add("<em>@hit</em>")
    file.add("</div>", -1)

    file
  }
}
