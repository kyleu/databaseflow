package services.scalaexport.db.file

import models.schema.ForeignKey
import models.scalaexport.db.{ExportField, ExportModel}
import models.scalaexport.file.OutputFile

object TwirlFormDefaultFields {
  def defaultField(field: ExportField, autocomplete: Option[(ForeignKey, ExportModel)], file: OutputFile) = {
    val prop = field.propertyName
    if (!field.notNull) {
      file.add("""<div class="nullable-input-container">""", 1)
    }
    autocomplete match {
      case Some(ac) =>
        file.add(s"""<div class="autocomplete" id="autocomplete-$prop">""", 1)
        file.add("""<div class="ac-input">""", 1)
        file.add("""<div class="input-field">""", 1)
        val url = s"@${ac._2.routesClass}.autocomplete()"
        val dataFields = s"""data-model="${ac._2.propertyName}" data-url="$url" data-activates="dropdown-$prop" data-beloworigin="true""""
        file.add(s"""<i class="fa @models.template.Icons.${ac._2.propertyName} prefix" title="${ac._2.title}"></i>""")
        if (field.notNull) {
          file.add(s"""<input id="input-$prop" class="lookup" $dataFields autocomplete="off" type="text" name="$prop" value="@model.$prop" />""")
        } else {
          file.add(s"""<input id="input-$prop" class="nullable lookup" $dataFields autocomplete="off" type="text" name="$prop" value="@model.$prop" />""")
        }
        file.add("</div>", -1)
        file.add("</div>", -1)
        file.add(s"""<ul id="dropdown-$prop" class="dropdown-content ac-dropdown"></ul>""")
        file.add("</div>", -1)
      case None => if (field.notNull) {
        file.add(s"""<input id="input-$prop" type="text" name="$prop" value="@model.$prop" />""")
      } else {
        file.add(s"""<input id="input-$prop" class="nullable" type="text" name="$prop" value="@model.$prop" />""")
      }
    }
    if (!field.notNull) {
      file.add(s"""<div class="nullable-link" id="nullable-$prop">@util.NullUtils.str</div>""")
      file.add("</div>", -1)
    }
  }
}
