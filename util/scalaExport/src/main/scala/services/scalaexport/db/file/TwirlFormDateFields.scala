package services.scalaexport.db.file

import models.scalaexport.db.ExportField
import models.scalaexport.file.OutputFile

object TwirlFormDateFields {
  def dateField(field: ExportField, file: OutputFile) = {
    val prop = field.propertyName
    file.add("""<div class="input-field">""", 1)
    file.add(s"""<i class="fa @models.template.Icons.date prefix"></i>""")
    file.add(s"""<input id="input-$prop" class="datepicker" autocomplete="off" type="text" name="$prop" value="@model.$prop" />""")
    file.add("</div>", -1)
  }

  def timeField(field: ExportField, file: OutputFile) = {
    val prop = field.propertyName
    file.add("""<div class="input-field">""", 1)
    file.add(s"""<i class="fa @models.template.Icons.time prefix"></i>""")
    file.add(s"""<input id="input-$prop" class="timepicker" autocomplete="off" type="text" name="$prop" value="@model.$prop" />""")
    file.add("</div>", -1)
  }

  def timestampField(field: ExportField, file: OutputFile) = {
    val prop = field.propertyName
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
  }
}
