package services.scalaexport.file

import models.scalaexport.OutputFile
import services.scalaexport.config.{ExportEnum, ExportField}

object TwirlFormEnumFields {
  def enumField(field: ExportField, enum: ExportEnum, file: OutputFile) = {
    val prop = field.propertyName
    file.add("""<div class="input-field">""", 1)
    file.add(s"""<select id="input-$prop" name="$prop">""", 1)
    if (field.nullable) {
      file.add(s"""<option value="@util.NullUtils.str" @if(model.result.isEmpty) { selected="selected" }>@util.NullUtils.str (null)</option>""")
    }
    file.add(s"""@${enum.modelPackage.mkString(".")}.${enum.className}.values.map { v =>""", 1)
    file.add(s"""<option @if(model.result.contains(v)) { selected="selected" } value="@v.value">@v.value</option>""")
    file.add("}", -1)
    file.add("</select>", -1)
    file.add("</div>", -1)
  }
}
