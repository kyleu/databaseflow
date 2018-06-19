package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.TwirlFile

object TwirlFormFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = TwirlFile(model.viewPackage, model.propertyName + "Form")

    val interpArgs = model.pkFields.map(f => "${model." + f.propertyName + "}").mkString(", ")
    val viewArgs = model.pkFields.map(f => "model." + f.propertyName).mkString(", ")

    file.add(s"@(user: models.user.SystemUser, model: ${model.modelClass}, title: String, cancel: Call, act: Call, isNew: Boolean = false, debug: Boolean = false)(")
    file.add(s"    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: ${config.providedPrefix}util.tracing.TraceData")
    file.add(s""")@traceData.logViewClass(getClass)@views.html.admin.layout.page(user, "explore", title) {""", 1)

    file.add(s"""<form id="form-edit-${model.propertyName}" action="@act" method="post">""", 1)
    file.add("""<div class="collection with-header">""", 1)

    file.add("<div class=\"collection-header\">", 1)
    file.add(s"""<div class="right"><button type="submit" class="btn theme">@if(isNew) {Create} else {Save} ${model.title}</button></div>""")
    file.add(s"""<div class="right"><a href="@cancel" class="theme-text cancel-link">Cancel</a></div>""")
    file.add(s"""<h5>${model.iconHtml(config.providedPrefix)} @title</h5>""")
    file.add("</div>", -1)

    file.add("<div class=\"collection-item\">", 1)
    file.add("<table>", 1)
    file.add("<tbody>", 1)

    model.fields.foreach { field =>
      val autocomplete = model.foreignKeys.find(_.references.forall(_.source == field.columnName)).map(x => x -> config.getModel(x.targetTable))
      TwirlFormFields.fieldFor(config.providedPrefix, model, field, file, autocomplete)
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
