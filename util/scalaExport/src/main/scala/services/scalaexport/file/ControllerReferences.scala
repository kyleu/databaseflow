package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportConfiguration, ExportModel}

object ControllerReferences {
  def write(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    if (model.references.nonEmpty) {
      val pkRefs = model.pkFields.map(_.propertyName).mkString(", ")
      val pkArgs = model.pkFields.map(x => s"${x.propertyName}: ${x.t.asScalaFull}").mkString(", ")
      val refServices = model.references.map(ref => (ref, config.getModel(ref.srcTable), config.getModel(ref.srcTable).getField(ref.srcCol)))

      file.add()
      file.add(s"""def relationCounts($pkArgs) = withSession("relation.counts", admin = true) { implicit request => implicit td =>""", 1)
      refServices.foreach { r =>
        file.add(s"val ${r._2.propertyName}By${r._3.className}F = ${r._2.propertyName}S.countBy${r._3.className}($pkRefs)")
      }
      val forArgs = refServices.map(r => s"${r._2.propertyName}C <- ${r._2.propertyName}By${r._3.className}F").mkString("; ")
      file.add(s"for($forArgs) yield {", 1)

      val mapArgs = refServices.map(r => s""""${r._2.propertyName}" -> ${r._2.propertyName}C""").mkString(", ")
      file.add(s"Ok(Map($mapArgs).asJson.spaces2).as(JSON)")
      file.add("}", -1)
      file.add("}", -1)
    }
  }

  def refServiceArgs(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val refServices = model.references.map(_.srcTable).distinct.map(config.getModel)
    refServices.foreach(s => file.addImport(s.servicePackage.mkString("."), s.className + "Service"))
    refServices.map(s => ", " + s.propertyName + "S: " + s.className + "Service").mkString
  }
}
