package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.config.ExportModel

object TwirlDataRowFile {
  def export(model: ExportModel) = {
    val searchColumns = model.fields.filter(_.inSearch)

    val listFile = TwirlFile(model.viewPackage, model.propertyName + "DataRow")
    listFile.add(s"@(model: ${model.modelClass})<tr>", 1)
    searchColumns.foreach { c =>
      val href = model.pkFields match {
        case Nil => ""
        case fields =>
          val args = fields.map(f => s"model.${f.propertyName}").mkString(", ")
          s"""@${model.routesClass}.view($args)"""
      }
      if (model.pkFields.exists(pkField => pkField.propertyName == c.propertyName)) {
        listFile.add(s"""<td><a href="$href" class="theme-text">@model.${c.propertyName}</a></td>""")
      } else {
        listFile.add(s"<td>@model.${c.propertyName}</td>")
      }
    }
    listFile.add("</tr>", -1)
    listFile
  }
}
