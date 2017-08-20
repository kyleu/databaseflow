package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.config.ExportModel

object TwirlViewFile {
  def export(model: ExportModel) = {
    val href = model.pkFields match {
      case Nil => ""
      case fields =>
        val args = fields.map(field => s"model.${field.propertyName}").mkString(", ")
        s"""@${model.routesClass}.editForm($args)"""
    }

    val viewFile = TwirlFile(model.viewPackage, model.propertyName + "View")
    viewFile.add(s"@(user: models.user.User, model: ${model.modelClass})(")
    viewFile.add("    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    val toInterp = model.pkFields.map(c => "${model." + c.propertyName + "}").mkString(", ")
    viewFile.add(s""")@traceData.logViewClass(getClass)@layout.admin(user, "explore", s"${model.title} [$toInterp]") {""", 1)

    viewFile.add("""<div class="collection with-header">""", 1)
    viewFile.add("<div class=\"collection-header\">", 1)

    viewFile.add(s"""<div class="right"><a class="theme-text" href="$href">Edit</a></div>""")
    viewFile.add("<h5>", 1)
    viewFile.add(s"""<i class="fa @models.template.Icons.${model.propertyName}"></i>""")
    val toTwirl = model.pkFields.map(c => "@model." + c.propertyName).mkString(", ")
    viewFile.add(s"""${model.title} [$toTwirl]""")
    viewFile.add("</h5>", -1)
    viewFile.add("</div>", -1)

    viewFile.add("<div class=\"collection-item\">", 1)
    viewFile.add("<table class=\"highlight\">", 1)
    viewFile.add("<tbody>", 1)
    model.fields.foreach { field =>
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
