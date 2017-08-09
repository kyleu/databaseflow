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

    val searchColumns = et.config.searchColumns.getOrElse(et.propertyName, et.pkColumns.map(x => x.name -> x.name))

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
        case cols =>
          val args = cols.map(col => s"model.${ExportHelper.toIdentifier(col.name)}").mkString(", ")
          s"""@$controllerClass.view($args)"""
      }
      if (et.pkColumns.exists(pk => ExportHelper.toClassName(pk.name) == ExportHelper.toClassName(c._1))) {
        listFile.add(s"""<td><a href="$href" class="theme-text">@model.${ExportHelper.toIdentifier(c._1)}</a></td>""")
      } else {
        listFile.add(s"<td>@model.${ExportHelper.toIdentifier(c._1)}</td>")
      }
    }
    listFile.add("</tr>", -1)
    listFile.add(s"}", -1)
    listFile.add()

    listFile.add("@views.html.admin.explore.list(", 1)
    listFile.add("user = user,")
    listFile.add(s"""model = "${et.title}",""")
    listFile.add(s"""modelPlural = "${et.plural}",""")
    listFile.add(s"icon = models.template.Icons.${et.propertyName},")
    listFile.add("cols = Seq(", 1)
    searchColumns.foreach {
      case c if searchColumns.lastOption.contains(c) => listFile.add(s""""${c._1}" -> "${c._2}"""")
      case c => listFile.add(s""""${c._1}" -> "${c._2}",""")
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
