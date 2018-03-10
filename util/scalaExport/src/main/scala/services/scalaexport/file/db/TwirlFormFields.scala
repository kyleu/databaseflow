package services.scalaexport.file.db

import models.scalaexport.OutputFile
import models.schema.{ColumnType, ForeignKey}
import services.scalaexport.config.{ExportField, ExportModel}

object TwirlFormFields {
  def inputFor(model: ExportModel, field: ExportField, file: OutputFile, autocomplete: Option[(ForeignKey, ExportModel)]) = {
    field.t match {
      case ColumnType.EnumType =>
        val enum = field.enumOpt.getOrElse(throw new IllegalStateException(s"Cannot find enum with name [${field.sqlTypeName}]."))
        TwirlFormEnumFields.enumField(field, enum, file)
      case ColumnType.CodeType => codeField(field, file)
      case ColumnType.BooleanType => booleanField(field, file)
      case ColumnType.DateType => TwirlFormDateFields.dateField(field, file)
      case ColumnType.TimeType => TwirlFormDateFields.timeField(field, file)
      case ColumnType.TimestampType => TwirlFormDateFields.timestampField(field, file)
      case _ => TwirlFormDefaultFields.defaultField(field, autocomplete, file)
    }
  }

  private[this] def codeField(field: ExportField, file: OutputFile) = {
    val prop = field.propertyName
    file.add("""<div class="input-field">""", 1)
    file.add(s"""<textarea id="input-$prop" name="$prop" class="materialize-textarea" style="font-family: monospace, monospace;">@model.$prop</textarea>""")
    file.add("</div>", -1)
  }

  private[this] def booleanField(field: ExportField, file: OutputFile) = {
    val prop = field.propertyName
    if (field.notNull) {
      file.add(s"""<input id="input-$prop-true" type="radio" name="$prop" value="true" @if(model.$prop) { checked="checked" } />""")
      file.add(s"""<label class="bool-radio-label" for="input-$prop-true">True</label>""")
      file.add(s"""<input id="input-$prop-false" type="radio" name="$prop" value="false" @if(!model.$prop) { checked="checked" } />""")
      file.add(s"""<label class="bool-radio-label" for="input-$prop-false">False</label>""")
    } else {
      file.add(s"""<input id="input-$prop-true" type="radio" name="$prop" value="true" @if(model.$prop.contains(true)) { checked="checked" } />""")
      file.add(s"""<label class="bool-radio-label" for="input-$prop-true">True</label>""")
      file.add(s"""<input id="input-$prop-false" type="radio" name="$prop" value="false" @if(model.$prop.contains(false)) { checked="checked" } />""")
      file.add(s"""<label class="bool-radio-label" for="input-$prop-false">False</label>""")
      file.add(s"""<input id="input-$prop-null" type="radio" name="$prop" value="null" @if(model.$prop.isEmpty) { checked="checked" } />""")
      file.add(s"""<label class="bool-radio-label" for="input-$prop-null">Null</label>""")
    }
  }
}
