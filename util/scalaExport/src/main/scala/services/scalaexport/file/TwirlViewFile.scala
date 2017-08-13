package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportConfiguration

object TwirlViewFile {
  def export(et: ExportConfiguration.Model) = {
    val pkg = "views" +: "admin" +: et.pkg
    val modelClass = et.pkg match {
      case Nil => s"models.${et.className}"
      case _ => s"models.${et.pkg.mkString(".")}.${et.className}"
    }

    val controllerClass = et.pkg match {
      case Nil => s"controllers.admin.routes.${et.className}Controller"
      case _ => s"controllers.admin.${et.pkg.mkString(".")}.routes.${et.className}Controller"
    }

    val href = et.pkColumns match {
      case Nil => ""
      case cols =>
        val args = cols.map(col => s"model.${ExportHelper.toIdentifier(col.name)}").mkString(", ")
        s"""@$controllerClass.formEdit($args)"""
    }

    val viewFile = TwirlFile(pkg, et.propertyName + "View")
    viewFile.add(s"@(user: models.user.User, model: $modelClass)(")
    viewFile.add("    implicit request: Request[AnyContent], session: Session, flash: Flash")
    val toInterp = et.pkColumns.map(c => "${model." + ExportHelper.toIdentifier(c.name) + "}").mkString(", ")
    viewFile.add(s""")@layout.admin(user, "explore", s"${et.title} [$toInterp]") {""", 1)

    viewFile.add("""<div class="collection with-header">""", 1)
    viewFile.add("<div class=\"collection-header\">", 1)

    viewFile.add(s"""<div class="right"><a class="theme-text" href="$href">Edit</a></div>""")
    viewFile.add("<h5>", 1)
    viewFile.add(s"""<i class="fa @models.template.Icons.${et.propertyName}"></i>""")
    val toTwirl = et.pkColumns.map(c => "@model." + ExportHelper.toIdentifier(c.name)).mkString(", ")
    viewFile.add(s"""${et.title} [$toTwirl]""")
    viewFile.add("</h5>", -1)
    viewFile.add("</div>", -1)

    viewFile.add("<div class=\"collection-item\">", 1)
    viewFile.add("<table class=\"highlight\">", 1)
    viewFile.add("<tbody>", 1)
    et.fields.foreach { field =>
      viewFile.add(s"<tr><th>${field.title}</th><td>@model.${field.propertyName}</td></tr>")
    }
    viewFile.add("</tbody>", -1)
    viewFile.add("</table>", -1)
    viewFile.add("</div>", -1)

    viewFile.add("</div>", -1)

    viewFile.add("}", -1)

    viewFile
  }
}
