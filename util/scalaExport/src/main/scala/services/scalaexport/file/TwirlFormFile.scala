package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.config.ExportModel

object TwirlFormFile {
  def export(model: ExportModel) = {
    val formFile = TwirlFile(model.viewPackage, model.propertyName + "Form")

    val interpArgs = model.pkFields.map(f => "${model." + f.propertyName + "}").mkString(", ")
    val viewArgs = model.pkFields.map(f => "model." + f.propertyName).mkString(", ")

    formFile.add(s"@(user: models.user.User, model: ${model.modelClass}, title: String, act: Call, isNew: Boolean = false)(")
    formFile.add("    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    formFile.add(s""")@traceData.logViewClass(getClass)@layout.admin(user, "explore", title) {""", 1)

    formFile.add(s"""<form action="@act" method="post">""", 1)
    formFile.add("""<div class="collection with-header">""", 1)

    formFile.add("<div class=\"collection-header\">", 1)
    formFile.add("<div class=\"right\">", 1)
    formFile.add(s"""<a class="right btn-flat theme-text" href="@${model.routesClass}.view($viewArgs)">Cancel</a>""")
    formFile.add(s"""<button type="submit" class="btn theme">@if(isNew) {Create} else {Save} ${model.title}</button>""")
    formFile.add("</div>", -1)
    formFile.add(s"""<h5><i class="fa @models.template.Icons.${model.propertyName}"></i> @title</h5>""")
    formFile.add("</div>", -1)

    formFile.add("<div class=\"collection-item\">", 1)
    formFile.add("<table class=\"highlight\">", 1)
    formFile.add("<tbody>", 1)

    model.fields.foreach { field =>
      formFile.add("<tr>", 1)
      formFile.add("<td>", 1)
      val inputFields = s"""type="checkbox" name="${field.propertyName}.include" id="${field.propertyName}.include" value="true""""
      if (model.pkFields.exists(_.columnName == field.columnName)) {
        formFile.add(s"""<input $inputFields @if(isNew) { checked="checked" } />""")
      } else if (field.notNull) {
        formFile.add(s"""<input $inputFields @if(isNew) { checked="checked" } />""")
      } else {
        formFile.add(s"""<input $inputFields />""")
      }
      formFile.add(s"""<label for="${field.propertyName}.include">${field.title}</label>""")
      formFile.add("</td>", -1)

      formFile.add("<td>", 1)
      TwirlFormFields.inputFor(field, formFile)
      formFile.add(s"</td>", -1)
      formFile.add("</tr>", -1)
    }

    formFile.add("</tbody>", -1)
    formFile.add("</table>", -1)
    formFile.add("</div>", -1)

    formFile.add("</div>", -1)
    formFile.add("</form>", -1)

    formFile.add("}", -1)

    formFile
  }
}
