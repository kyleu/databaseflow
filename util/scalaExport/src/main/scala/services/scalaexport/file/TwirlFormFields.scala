package services.scalaexport.file

import models.scalaexport.OutputFile
import models.schema.{ColumnType, ForeignKey}
import services.scalaexport.config.{ExportField, ExportModel}

object TwirlFormFields {
  def inputFor(model: ExportModel, field: ExportField, file: OutputFile, autocomplete: Option[(ForeignKey, ExportModel)]) = {
    val prop = field.propertyName
    field.t match {
      case ColumnType.CodeType =>
        file.add("""<div class="input-field">""", 1)
        file.add(s"""<textarea id="input-$prop" name="$prop" class="materialize-textarea" style="font-family: monospace, monospace;">@model.$prop</textarea>""")
        file.add("</div>", -1)
      case ColumnType.BooleanType => if (field.notNull) {
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
      case ColumnType.DateType =>
        file.add("""<div class="input-field">""", 1)
        file.add(s"""<i class="fa @models.template.Icons.date prefix"></i>""")
        file.add(s"""<input id="input-$prop" class="datepicker" autocomplete="off" type="text" name="$prop" value="@model.$prop" />""")
        file.add("</div>", -1)
      case ColumnType.TimeType =>
        file.add("""<div class="input-field">""", 1)
        file.add(s"""<i class="fa @models.template.Icons.time prefix"></i>""")
        file.add(s"""<input id="input-$prop" class="timepicker" autocomplete="off" type="text" name="$prop" value="@model.$prop" />""")
        file.add("</div>", -1)
      case ColumnType.TimestampType =>
        file.add("""<div class="input-field" style="width: 50%; float: left;">""", 1)
        file.add(s"""<i class="fa @models.template.Icons.date prefix"></i>""")
        val dateTransform = if (field.notNull) { s"@model.$prop.toLocalDate" } else { s"@model.$prop.map(_.toLocalDate)" }
        file.add(s"""<input id="input-$prop-date" class="datepicker" autocomplete="off" type="text" name="$prop-date" value="$dateTransform" />""")
        file.add("</div>", -1)
        file.add("""<div class="input-field" style="width: 50%; float: right;">""", 1)
        file.add(s"""<i class="fa @models.template.Icons.time prefix"></i>""")
        val timeTransform = if (field.notNull) { s"@model.$prop.toLocalTime" } else { s"@model.$prop.map(_.toLocalTime)" }
        file.add(s"""<input id="input-$prop-time" class="timepicker" autocomplete="off" type="text" name="$prop-time" value="$timeTransform" />""")
        file.add("</div>", -1)
      case _ => autocomplete match {
        case Some(ac) =>
          file.add(s"""<div class="autocomplete" id="autocomplete-$prop">""", 1)
          file.add("""<div class="ac-input">""", 1)
          file.add("""<div class="input-field">""", 1)
          val url = s"@${ac._2.routesClass}.autocomplete()"
          val dataFields = s"""data-model="${ac._2.propertyName}" data-url="$url" data-activates="dropdown-$prop" data-beloworigin="true""""
          file.add(s"""<i class="fa @models.template.Icons.${ac._2.propertyName} prefix" title="${ac._2.title}"></i>""")
          file.add(s"""<input id="input-$prop" class="lookup" $dataFields autocomplete="off" type="text" name="$prop" value="@model.$prop" />""")
          file.add("</div>", -1)
          file.add("</div>", -1)
          file.add(s"""<ul id="dropdown-$prop" class="dropdown-content ac-dropdown"></ul>""")
          file.add("</div>", -1)
        case None => file.add(s"""<input id="input-$prop" type="text" name="$prop" value="@model.$prop" />""")
      }
    }
  }
}
