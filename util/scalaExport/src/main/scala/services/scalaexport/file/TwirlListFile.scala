package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.config.ExportModel

object TwirlListFile {
  def export(model: ExportModel) = {
    val searchColumns = model.fields.filter(_.inSearch)

    val listFile = TwirlFile(model.viewPackage, model.propertyName + "List")
    val viewArgs = "user: models.user.User, q: Option[String], orderBy: Option[String], orderAsc: Boolean, totalCount: Option[Int]"

    listFile.add(s"@($viewArgs, modelSeq: Seq[${model.modelClass}], limit: Int, offset: Int)(", 2)
    listFile.add("implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    listFile.add(s")", -2)
    listFile.add("@traceData.logViewClass(getClass)")
    listFile.add()
    listFile.add(s"@resultFor(model: ${model.modelClass}) = {", 1)
    listFile.add("<tr>", 1)
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
    listFile.add(s"}", -1)
    listFile.add()

    listFile.add("@views.html.admin.explore.list(", 1)
    listFile.add("user = user,")
    listFile.add(s"""model = "${model.title}",""")
    listFile.add(s"""modelPlural = "${model.plural}",""")
    listFile.add(s"icon = models.template.Icons.${model.propertyName},")
    listFile.add("cols = Seq(", 1)
    searchColumns.foreach {
      case c if searchColumns.lastOption.contains(c) => listFile.add(s""""${c.propertyName}" -> "${c.title}"""")
      case c => listFile.add(s""""${c.propertyName}" -> "${c.title}",""")
    }
    listFile.add("),", -1)
    listFile.add("orderBy = orderBy,")
    listFile.add("orderAsc = orderAsc,")
    listFile.add("totalCount = totalCount,")
    listFile.add("rows = modelSeq.map(resultFor),")
    listFile.add(s"newUrl = Some(${model.routesClass}.createForm()),")
    listFile.add(s"orderByUrl = Some(${model.routesClass}.list(q, _, _, Some(limit), Some(0))),")
    listFile.add(s"searchUrl = Some(${model.routesClass}.list(None, orderBy, orderAsc, Some(limit), None)),")
    listFile.add(s"nextUrl = ${model.routesClass}.list(q, None, Some(limit), Some(offset + limit)),")
    listFile.add("limit = limit,")
    listFile.add("offset = offset,")
    listFile.add("q = q")
    listFile.add(")", -1)

    listFile
  }
}
