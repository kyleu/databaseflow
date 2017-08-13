package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportModel

object TwirlListFile {
  def export(model: ExportModel) = {
    val pkg = "views" +: "admin" +: model.pkg
    val modelClass = model.pkg match {
      case Nil => s"models.${model.className}"
      case _ => s"models.${model.pkg.mkString(".")}.${model.className}"
    }
    val controllerClass = model.pkg match {
      case Nil => s"controllers.admin.routes.${model.className}Controller"
      case _ => s"controllers.admin.${model.pkg.mkString(".")}.routes.${model.className}Controller"
    }

    val searchColumns = model.fields.filter(_.inSearch)

    val listFile = TwirlFile(pkg, model.propertyName + "List")
    val viewArgs = "user: models.user.User, q: Option[String], orderBy: Option[String], orderAsc: Boolean, totalCount: Option[Int]"

    listFile.add(s"@($viewArgs, modelSeq: Seq[$modelClass], limit: Int, offset: Int)(", 2)
    listFile.add("implicit request: Request[AnyContent], session: Session, flash: Flash")
    listFile.add(s")", -2)
    listFile.add()
    listFile.add(s"@resultFor(model: $modelClass) = {", 1)
    listFile.add("<tr>", 1)
    searchColumns.foreach { c =>
      val href = model.pkColumns match {
        case Nil => ""
        case cols =>
          val args = cols.map(col => s"model.${ExportHelper.toIdentifier(col.name)}").mkString(", ")
          s"""@$controllerClass.view($args)"""
      }
      if (model.pkColumns.exists(pkCol => pkCol.name == c.columnName)) {
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
    listFile.add(s"newUrl = Some($controllerClass.createForm()),")
    listFile.add(s"orderByUrl = Some($controllerClass.list(q, _, _, Some(limit), Some(0))),")
    listFile.add(s"searchUrl = Some($controllerClass.list(None, orderBy, orderAsc, Some(limit), None)),")
    listFile.add(s"nextUrl = $controllerClass.list(q, None, Some(limit), Some(offset + limit)),")
    listFile.add("limit = limit,")
    listFile.add("offset = offset,")
    listFile.add("q = q")
    listFile.add(")", -1)

    listFile
  }
}
