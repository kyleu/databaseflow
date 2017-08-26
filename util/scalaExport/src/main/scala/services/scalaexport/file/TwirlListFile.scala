package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.config.ExportModel

object TwirlListFile {
  def export(model: ExportModel) = {
    val searchColumns = model.fields.filter(_.inSearch)

    val listFile = TwirlFile(model.viewPackage, model.propertyName + "List")
    val viewArgs = "q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Int, offset: Int"

    listFile.add(s"@(user: models.user.User, totalCount: Option[Int], modelSeq: Seq[${model.modelClass}], $viewArgs)(", 2)
    listFile.add("implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    listFile.add(s")@traceData.logViewClass(getClass)", -2)
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
    listFile.add("totalCount = totalCount,")
    listFile.add(s"rows = modelSeq.map(model => ${model.viewHtmlPackage.mkString(".")}.${model.propertyName}DataRow(model)),")
    listFile.add("orderBy = orderBy,")
    listFile.add("orderAsc = orderAsc,")
    listFile.add(s"newUrl = Some(${model.routesClass}.createForm()),")
    listFile.add(s"orderByUrl = Some(${model.routesClass}.list(q, _, _, Some(limit), Some(0))),")
    listFile.add(s"searchUrl = Some(${model.routesClass}.list(None, orderBy, orderAsc, Some(limit), None)),")
    listFile.add(s"nextUrl = ${model.routesClass}.list(q, orderBy, orderAsc, Some(limit), Some(offset + limit)),")
    listFile.add("limit = limit,")
    listFile.add("offset = offset,")
    listFile.add("q = q")
    listFile.add(")", -1)

    listFile
  }
}
