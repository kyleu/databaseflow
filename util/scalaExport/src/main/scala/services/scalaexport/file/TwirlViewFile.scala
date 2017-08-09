package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.{ExportHelper, ExportTable}

object TwirlViewFile {
  def export(et: ExportTable) = {
    val pkg = "views" +: "admin" +: et.pkg :+ et.propertyName
    val modelClass = et.pkg match {
      case Nil => s"models.${et.className}"
      case _ => s"models.${et.pkg.mkString(".")}.${et.className}"
    }

    val viewFile = TwirlFile(pkg, "view" + et.className)
    viewFile.add(s"@(user: models.user.User, model: $modelClass)(")
    viewFile.add("    implicit request: Request[AnyContent], session: Session, flash: Flash")
    val toInterp = et.pkColumns.map(c => "${model." + ExportHelper.toIdentifier(c.name) + "}").mkString(", ")
    viewFile.add(s""")@layout.admin(user, "explore", s"${et.title} [$toInterp]") {""", 1)

    viewFile.add("""<div class="collection with-header">""", 1)
    viewFile.add("<div class=\"collection-header\">", 1)
    viewFile.add("<h5>", 1)
    viewFile.add(s"""<i class="fa @models.template.Icons.${et.propertyName}"></i>""")
    val toTwirl = et.pkColumns.map(c => "@model." + ExportHelper.toIdentifier(c.name)).mkString(", ")
    viewFile.add(s"""${et.title} [$toTwirl]""")
    viewFile.add("</h5>", -1)
    viewFile.add("</div>", -1)

    viewFile.add("<div class=\"collection-item\">", 1)
    viewFile.add("<table class=\"highlight\">", 1)
    viewFile.add("<tbody>", 1)
    et.t.columns.foreach { col =>
      val label = ExportHelper.toIdentifier(col.name)
      viewFile.add(s"<tr><th>$label</th><td>@model.$label</td></tr>")
    }
    viewFile.add("</tbody>", -1)
    viewFile.add("</table>", -1)
    viewFile.add("</div>", -1)

    viewFile.add("</div>", -1)

    viewFile.add("}", -1)

    viewFile
  }
}
