package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.{ExportHelper, ExportTable}

object TwirlListFile {
  def export(et: ExportTable) = {
    val pkg = "views" +: "admin" +: et.pkg :+ et.propertyName
    val modelClass = et.pkg match {
      case Nil => s"models.${et.className}"
      case _ => s"models.${et.pkg.mkString(".")}.${et.className}"
    }
    val controllerClass = et.pkg match {
      case Nil => s"controllers.admin.routes.${et.className}Controller"
      case _ => s"controllers.admin.${et.pkg.mkString(".")}.routes.${et.className}Controller"
    }

    val searchColumns = et.config.searchColumns.getOrElse(et.propertyName, et.pkColumns.map(_.name))

    val listFile = TwirlFile(pkg, "list" + et.className)

    listFile.add(s"@(user: models.user.User, q: Option[String], modelSeq: Seq[$modelClass], limit: Int, offset: Int)(", 2)
    listFile.add("implicit request: Request[AnyContent], session: Session, flash: Flash")
    listFile.add(s")", -2)
    listFile.add()
    listFile.add(s"@resultFor(model: $modelClass) = {", 1)
    listFile.add("<tr>", 1)
    searchColumns.foreach { c =>
      val href = et.pkColumns match {
        case Nil => ""
        case h :: Nil => s"""@$controllerClass.view(model.${ExportHelper.toIdentifier(h.name)})"""
        case cols =>
          val args = cols.map(c => s"model.${ExportHelper.toIdentifier(c.name)}").mkString(", ")
          s"""@$controllerClass.view($args)"""
      }
      if (et.pkColumns.exists(pk => ExportHelper.toClassName(pk.name) == ExportHelper.toClassName(c))) {
        listFile.add(s"""<td><a href="$href" class="theme-text">@model.${ExportHelper.toIdentifier(c)}</a></td>""")
      } else {
        listFile.add(s"<td>@model.${ExportHelper.toIdentifier(c)}</td>")
      }
    }
    listFile.add("</tr>", -1)
    listFile.add(s"}", -1)
    listFile.add()

    listFile.add("@views.html.admin.explore.list(", 1)
    listFile.add("user = user,")
    listFile.add(s"""model = "${et.className}",""")
    listFile.add(s"""modelPlural = "${et.className} Models",""")
    listFile.add(s"icon = models.template.Icons.${et.propertyName},")
    listFile.add("cols = Seq(", 1)
    searchColumns.foreach {
      case c if searchColumns.lastOption.contains(c) => listFile.add(s""""$c" -> "${ExportHelper.toClassName(c)}"""")
      case c => listFile.add(s""""$c" -> "${ExportHelper.toClassName(c)}",""")
    }
    listFile.add("),", -1)
    listFile.add("rows = modelSeq.map(resultFor),")
    listFile.add(s"nextUrl = $controllerClass.list(q, Some(limit), Some(offset + limit)),")
    listFile.add("limit = limit,")
    listFile.add("offset = offset,")
    listFile.add("showSearch = true,")
    listFile.add("q = q")
    listFile.add(")", -1)

    listFile
  }
}
