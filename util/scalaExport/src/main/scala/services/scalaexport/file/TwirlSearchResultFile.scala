package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.config.ExportModel

object TwirlSearchResultFile {
  def export(model: ExportModel) = {
    val listFile = TwirlFile(model.viewPackage, model.propertyName + "SearchResult")

    listFile.add(s"""@(model: ${model.modelClass}, hit: String)<div class="search-result">""", 1)
    listFile.add(s"""<div class="right">${model.title}</div>""")
    listFile.add("<div>", 1)
    listFile.add(s"""<i class="fa @models.template.Icons.${model.propertyName}"></i>""")
    if (model.pkFields.isEmpty) {
      listFile.add("@model")
    } else {
      val cs = model.pkFields.map(f => "model." + f.propertyName)
      listFile.add(s"""<a class="theme-text" href="@${model.routesClass}.view(${cs.mkString(", ")})">${cs.map("@" + _).mkString(", ")}</a>""")
    }
    listFile.add("</div>", -1)
    listFile.add("<em>@hit</em>")
    listFile.add("</div>", -1)

    listFile
  }
}
