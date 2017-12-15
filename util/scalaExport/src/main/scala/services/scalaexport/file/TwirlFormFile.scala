package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.config.{ExportConfiguration, ExportModel}

object TwirlFormFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(model.viewPackage, model.propertyName + "Form")

    val interpArgs = model.pkFields.map(f => "${model." + f.propertyName + "}").mkString(", ")
    val viewArgs = model.pkFields.map(f => "model." + f.propertyName).mkString(", ")

    file.add(s"@(user: models.user.SystemUser, model: ${model.modelClass}, title: String, cancel: Call, act: Call, isNew: Boolean = false, debug: Boolean = false)(")
    file.add("    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    file.add(s""")@traceData.logViewClass(getClass)@views.html.admin.layout.page(user, "explore", title) {""", 1)

    file.add(s"""<form id="form-edit-${model.propertyName}" action="@act" method="post">""", 1)
    file.add("""<div class="collection with-header">""", 1)

    file.add("<div class=\"collection-header\">", 1)
    file.add(s"""<div class="right"><button type="submit" class="btn theme">@if(isNew) {Create} else {Save} ${model.title}</button></div>""")
    file.add(s"""<div class="right"><a href="@cancel" class="theme-text cancel-link">Cancel</a></div>""")
    file.add(s"""<h5>${model.iconHtml} @title</h5>""")
    file.add("</div>", -1)

    file.add("<div class=\"collection-item\">", 1)
    file.add("<table>", 1)
    file.add("<tbody>", 1)

    model.fields.foreach { field =>
      file.add("<tr>", 1)
      file.add("<td>", 1)
      val inputProps = s"""type="checkbox" name="${field.propertyName}.include" id="${field.propertyName}.include" value="true""""
      val dataProps = if (field.notNull) {
        s"""class="data-input" data-type="${field.t}" data-name="${field.propertyName}""""
      } else {
        s"""class="data-input nullable" data-type="${field.t}" data-name="${field.propertyName}""""
      }
      file.add(s"""<input $inputProps @if(isNew) { checked="checked" } $dataProps />""")
      file.add(s"""<label for="${field.propertyName}.include">${field.title}</label>""")
      file.add("</td>", -1)

      file.add("<td>", 1)
      val autocomplete = model.foreignKeys.find(_.references.forall(_.source == field.columnName)).map(x => x -> config.getModel(x.targetTable))
      TwirlFormFields.inputFor(model, field, file, autocomplete)
      file.add(s"</td>", -1)
      file.add("</tr>", -1)
    }

    file.add("</tbody>", -1)
    file.add("</table>", -1)
    file.add("</div>", -1)

    file.add("</div>", -1)
    file.add("</form>", -1)

    file.add("}", -1)

    file.add("@views.html.components.includeScalaJs(debug)")
    file.add("@views.html.components.includeAutocomplete(debug)")
    file.add(s"""<script>$$(function() { new FormService('form-edit-${model.propertyName}'); })</script>""")

    file
  }
}
