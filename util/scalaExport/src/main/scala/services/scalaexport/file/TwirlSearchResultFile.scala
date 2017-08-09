package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.{ExportHelper, ExportTable}

object TwirlSearchResultFile {
  def export(et: ExportTable) = {
    val pkg = "views" +: "admin" +: et.pkg :+ et.propertyName

    val modelClass = et.pkg match {
      case Nil => s"models.${et.className}"
      case _ => s"models.${et.pkg.mkString(".")}.${et.className}"
    }

    val controllerClass = et.pkg match {
      case Nil => s"controllers.admin.routes.${et.className}Controller"
      case _ => s"controllers.admin.${et.pkg.mkString(".")}.routes.${et.className}Controller"
    }

    val listFile = TwirlFile(pkg, "searchResult" + et.className)

    listFile.add(s"""@(model: $modelClass, hit: String)<div class="search-result">""", 1)
    listFile.add(s"""<div class="right">${et.title}</div>""")
    listFile.add("<div>", 1)
    listFile.add(s"""<i class="fa @models.template.Icons.${et.propertyName}"></i>""")
    val cs = et.pkColumns.map(c => "model." + ExportHelper.toIdentifier(c.name))
    listFile.add(s"""<a class="theme-text" href="@$controllerClass.view(${cs.mkString(", ")})">${cs.map("@" + _).mkString(", ")}</a>""")
    listFile.add("</div>", -1)
    listFile.add("<em>@hit</em>")
    listFile.add("</div>", -1)

    listFile
  }
}
