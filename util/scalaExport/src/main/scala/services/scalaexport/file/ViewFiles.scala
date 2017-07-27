package services.scalaexport.file

import models.scalaexport.TwirlFile
import services.scalaexport.ExportTable

object ViewFiles {
  def export(et: ExportTable) = {
    val listFile = TwirlFile("views" +: "admin" +: et.pkg :+ et.propertyName, "list" + et.className)
    listFile.add(s"@(modelSeq: Seq[models.${et.pkg.mkString(".")}.${et.className}])")
    listFile.add(s"""<div class="collection with-header">""", 1)
    listFile.add(s"""<div class="collection-header">""", 1)
    listFile.add(s"<h5>@util.NumberUtils.withCommas(modelSeq.size) ${et.className} Objects</h5>")
    listFile.add(s"</div>", -1)
    listFile.add("@modelSeq.map { model =>", 1)
    listFile.add("""<a href="" class="collection-item">@model</a>""")
    listFile.add("}", -1)
    listFile.add("</div>", -1)

    val viewFile = TwirlFile("views" +: "admin" +: et.propertyName +: et.pkg, "view" + et.className)
    viewFile.add(s"@(model: models.${et.pkg.mkString(".")}.${et.className})")
    viewFile.add(s"Hello! Here's your ${et.className}:")
    viewFile.add("@model")

    Seq(listFile, viewFile)
  }
}
