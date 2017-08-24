package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.config.ExportModel

object TwirlRelationsFile {
  def export(model: ExportModel) = {
    val searchColumns = model.fields.filter(_.inSearch)

    val listFile = TwirlFile(model.viewPackage, model.propertyName + "Relations")
    val viewArgs = "user: models.user.User, relation: String, orderBy: Option[String], orderAsc: Boolean"

    listFile.add(s"@($viewArgs, modelSeq: Seq[${model.modelClass}], limit: Int, offset: Int)(", 2)
    listFile.add("implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    listFile.add(s")@traceData.logViewClass(getClass)", -2)
    listFile.add(s"<!-- @relation -->")
    listFile.add("<table>", 1)
    listFile.add("<thead>", 1)
    listFile.add("<tr>", 1)
    searchColumns.foreach { c =>
      listFile.add(s"<th>${c.title}</th>")
    }
    listFile.add("</tr>", -1)
    listFile.add("</thead>", -1)
    listFile.add("<tbody>", 1)
    listFile.add("@modelSeq.map { model =>", 1)
    listFile.add("<tr>", 1)
    searchColumns.foreach { c =>
      if (model.pkFields.exists(pkField => pkField.propertyName == c.propertyName)) {
        val href = model.pkFields match {
          case Nil => ""
          case fields =>
            val args = fields.map(f => s"model.${f.propertyName}").mkString(", ")
            s"""@${model.routesClass}.view($args)"""
        }
        listFile.add(s"""<td><a href="$href" class="theme-text">@model.${c.propertyName}</a></td>""")
      } else {
        listFile.add(s"<td>@model.${c.propertyName}</td>")
      }
    }
    listFile.add("</tr>", -1)
    listFile.add("}", -1)
    listFile.add("</tbody>", -1)
    listFile.add("</table>", -1)

    listFile
  }
}
