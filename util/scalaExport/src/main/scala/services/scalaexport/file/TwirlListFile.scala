package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.{ExportHelper, ExportTable}

object TwirlListFile {
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

    val searchColumns = et.config.searchColumns.getOrElse(et.propertyName, et.pkColumns.map(_.name))

    val listFile = TwirlFile(pkg, "list" + et.className)
    listFile.add(s"@(user: models.user.User, modelSeq: Seq[$modelClass])(")
    listFile.add("    implicit request: Request[AnyContent], session: Session, flash: Flash")
    listFile.add(s""")@layout.admin(user, "explore", s"${et.className} List") {""", 1)
    listFile.add("<div class=\"collection with-header\">", 1)
    listFile.add("<div class=\"collection-header\">", 1)
    listFile.add("<h5>", 1)
    listFile.add(s"""<i class="fa @models.template.Icons.${et.propertyName}"></i>""")
    listFile.add(s"""@util.NumberUtils.withCommas(modelSeq.size) ${et.className} Objects""")
    listFile.add("</h5>", -1)
    listFile.add("</div>", -1)

    listFile.add("<div class=\"collection-item\">", 1)
    listFile.add("<table class=\"bordered highlight\">", 1)
    listFile.add("<thead>", 1)
    listFile.add("<tr>", 1)
    searchColumns.foreach { c =>
      listFile.add(s"<th>${ExportHelper.toClassName(c)}</th>")
    }
    listFile.add("</tr>", -1)
    listFile.add("</thead>", -1)
    listFile.add("<tbody>", 1)
    listFile.add("@modelSeq.map { model =>", 1)
    listFile.add("<tr>", 1)
    val href = et.pkColumns match {
      case Nil => ""
      case h :: Nil => s"""@$controllerClass.view(model.${ExportHelper.toIdentifier(h.name)})"""
      case _ => "" // todo
    }
    searchColumns.foreach { c =>
      if (et.pkColumns.exists(pk => ExportHelper.toClassName(pk.name) == ExportHelper.toClassName(c))) {
        listFile.add(s"""<td><a href="$href" class="theme-text">@model.${ExportHelper.toIdentifier(c)}</a></td>""")
      } else {
        listFile.add(s"<td>@model.${ExportHelper.toIdentifier(c)}</td>")
      }
    }
    listFile.add("</tr>", -1)
    listFile.add("}", -1)
    listFile.add("</tbody>", -1)
    listFile.add("</table>", -1)
    listFile.add("</div>", -1)
    listFile.add("</div>", -1)
    listFile.add("}", -1)

    listFile
  }
}
