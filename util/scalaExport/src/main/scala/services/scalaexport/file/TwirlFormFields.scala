package services.scalaexport.file

import models.scalaexport.OutputFile
import models.schema.{ColumnType, ForeignKey}
import services.scalaexport.config.{ExportField, ExportModel}

object TwirlFormFields {
  def inputFor(model: ExportModel, field: ExportField, file: OutputFile, autocomplete: Option[(ForeignKey, ExportModel)]) = {
    val prop = field.propertyName
    field.t match {
      case ColumnType.BooleanType =>
        file.add("<div>", 1)
        if (field.notNull) {
          file.add(s"""<input id="input-$prop-true" type="radio" name="$prop" value="true" @if(model.$prop) { checked="checked" } />""")
          file.add(s"""<label for="input-$prop-true">True</label>""")
          file.add(s"""<input id="input-$prop-false" type="radio" name="$prop" value="false" @if(!model.$prop) { checked="checked" } />""")
          file.add(s"""<label for="input-$prop-false">False</label>""")
        } else {
          file.add(s"""<input id="input-$prop-true" type="radio" name="$prop" value="true" @if(model.$prop.contains(true)) { checked="checked" } />""")
          file.add(s"""<label for="input-$prop-true">True</label>""")
          file.add(s"""<input id="input-$prop-false" type="radio" name="$prop" value="false" @if(model.$prop.contains(false)) { checked="checked" } />""")
          file.add(s"""<label for="input-$prop-false">False</label>""")
          file.add(s"""<input id="input-$prop-null" type="radio" name="$prop" value="null" @if(model.$prop.isEmpty) { checked="checked" } />""")
          file.add(s"""<label for="input-$prop-null">Null</label>""")
        }
        file.add("</div>", -1)
      case _ => autocomplete match {
        case Some(ac) =>
          file.add(s"""<div class="autocomplete" id="autocomplete-$prop">""", 1)
          file.add("""<div class="ac-input">""", 1)
          val url = s"@${ac._2.routesClass}.autocomplete()"
          val dataFields = s"""data-model="${ac._2.propertyName}" data-url="$url" data-activates="dropdown-$prop" data-beloworigin="true""""
          file.add(s"""<input id="input-$prop" class="lookup" $dataFields autocomplete="off" type="text" name="author" value="@model.$prop" />""")
          file.add("</div>", -1)
          file.add(s"""<ul id="dropdown-$prop" class="dropdown-content ac-dropdown"></ul>""")
          file.add("</div>", -1)
        case None =>
          file.add(s"""<input id="input-$prop" type="text" name="$prop" value="@model.$prop" />""")
      }
    }
  }
}
