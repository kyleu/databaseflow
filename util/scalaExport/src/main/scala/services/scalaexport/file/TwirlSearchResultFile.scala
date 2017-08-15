package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportModel

object TwirlSearchResultFile {
  def export(model: ExportModel) = {
    val pkg = "views" +: "admin" +: model.pkg

    val modelClass = model.pkg match {
      case Nil => s"models.${model.className}"
      case _ => s"models.${model.pkg.mkString(".")}.${model.className}"
    }

    val controllerClass = model.pkg match {
      case Nil => s"controllers.admin.routes.${model.className}Controller"
      case _ => s"controllers.admin.${model.pkg.mkString(".")}.routes.${model.className}Controller"
    }

    val listFile = TwirlFile(pkg, model.propertyName + "SearchResult")

    listFile.add(s"""@(model: $modelClass, hit: String)<div class="search-result">""", 1)
    listFile.add(s"""<div class="right">${model.title}</div>""")
    listFile.add("<div>", 1)
    listFile.add(s"""<i class="fa @models.template.Icons.${model.propertyName}"></i>""")
    if (model.pkColumns.isEmpty) {
      listFile.add("@model")
    } else {
      val cs = model.pkColumns.map(c => "model." + ExportHelper.toIdentifier(c.name))
      listFile.add(s"""<a class="theme-text" href="@$controllerClass.view(${cs.mkString(", ")})">${cs.map("@" + _).mkString(", ")}</a>""")
    }
    listFile.add("</div>", -1)
    listFile.add("<em>@hit</em>")
    listFile.add("</div>", -1)

    listFile
  }
}
