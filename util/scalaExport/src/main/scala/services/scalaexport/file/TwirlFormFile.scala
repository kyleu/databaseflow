package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.{ExportHelper, ExportTable}

object TwirlFormFile {
  def export(et: ExportTable) = {
    val pkg = "views" +: "admin" +: et.pkg
    val modelClass = et.pkg match {
      case Nil => s"models.${et.className}"
      case _ => s"models.${et.pkg.mkString(".")}.${et.className}"
    }

    val formFile = TwirlFile(pkg, et.propertyName + "Form")
    formFile.add(s"@(user: models.user.User, model: $modelClass, isNew: Boolean = false)(")
    formFile.add("    implicit request: Request[AnyContent], session: Session, flash: Flash")
    val toInterp = et.pkColumns.map(c => "${model." + ExportHelper.toIdentifier(c.name) + "}").mkString(", ")
    formFile.add(s""")@layout.admin(user, "explore", s"${et.title} [$toInterp]") {""", 1)

    formFile.add("""<div class="collection with-header">""", 1)
    formFile.add("<div class=\"collection-header\">", 1)
    formFile.add("<h5>", 1)
    formFile.add(s"""<i class="fa @models.template.Icons.${et.propertyName}"></i>""")
    val toTwirl = et.pkColumns.map(c => "@model." + ExportHelper.toIdentifier(c.name)).mkString(", ")
    formFile.add(s"""${et.title} [$toTwirl]""")
    formFile.add("</h5>", -1)
    formFile.add("</div>", -1)

    formFile.add("<div class=\"collection-item\">", 1)
    formFile.add("<table class=\"highlight\">", 1)
    formFile.add("<tbody>", 1)
    et.t.columns.foreach { col =>
      val label = ExportHelper.toIdentifier(col.name)
      formFile.add("<tr>", 1)
      formFile.add(s"""<th>$label</th>""")
      formFile.add(s"""<td><input id="input-$label" type="text" name="$label" value="@model.$label" /></td>""")
      formFile.add("</tr>", -1)
    }
    formFile.add("</tbody>", -1)
    formFile.add("</table>", -1)
    formFile.add("</div>", -1)

    formFile.add("</div>", -1)

    formFile.add("}", -1)

    formFile
  }
}
