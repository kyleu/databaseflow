package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.ExportTable

object ViewFiles {
  def export(et: ExportTable) = {
    val pkg = "views" +: "admin" +: et.pkg :+ et.propertyName
    val modelClass = et.pkg match {
      case Nil => s"models.${et.className}"
      case _ => s"models.${et.pkg.mkString(".")}.${et.className}"
    }

    val listFile = TwirlFile(pkg, "list" + et.className)
    listFile.add(s"@(user: models.user.User, modelSeq: Seq[$modelClass])(")
    listFile.add("    implicit request: Request[AnyContent], session: Session, flash: Flash")
    listFile.add(s""")@layout.admin(user, "explore", s"${et.className} List") {""", 1)
    listFile.add("<div class=\"content-panel\">", 1)
    listFile.add("<div class=\"collection with-header\">", 1)
    listFile.add("<div class=\"collection-header\">", 1)
    listFile.add("<h5>", 1)
    listFile.add(s"""<i class="fa @models.template.Icons.${et.propertyName}"></i>""")
    listFile.add(s"""@util.NumberUtils.withCommas(modelSeq.size) ${et.className} Objects""")
    listFile.add("</h5>", -1)
    listFile.add("</div>", -1)
    listFile.add("@modelSeq.map { model =>", 1)
    listFile.add("<a href=\"\" class=\"collection-item\">@model</a>")
    listFile.add("}", -1)
    listFile.add("</div>", -1)
    listFile.add("</div>", -1)
    listFile.add("}", -1)

    val viewFile = TwirlFile(pkg, "view" + et.className)
    viewFile.add(s"@(user: models.user.User, model: $modelClass)(")
    viewFile.add("    implicit request: Request[AnyContent], session: Session, flash: Flash")
    viewFile.add(s""")@layout.admin(user, "explore", s"${et.className} Detail") {""", 1)
    viewFile.add(s"Hello! Here's your ${et.propertyName}:")
    viewFile.add("@model")
    viewFile.add("}", -1)

    Seq(listFile, viewFile)
  }
}
