package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportConfiguration, ExportModel}

object ControllerReferences {
  def write(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val references = model.transformedReferences(config)
    if (references.nonEmpty) {
      file.addImport("models.result", "RelationCount")
      val pkRefs = model.pkFields.map(_.propertyName).mkString(", ")
      val pkArgs = model.pkFields.map(x => s"${x.propertyName}: ${x.t.asScalaFull}").mkString(", ")
      val refServices = references.map(ref => (ref._2, ref._3, ref._4))

      file.add()
      file.add(s"""def relationCounts($pkArgs) = withSession("relation.counts", admin = true) { implicit request => implicit td =>""", 1)
      file.add(s"val creds = models.auth.Credentials.fromRequest(request)")
      refServices.foreach { r =>
        file.add(s"val ${r._2.propertyName}By${r._3.className} = ${r._2.propertyName}S.countBy${r._3.className}(creds, $pkRefs)")
      }
      file.add(s"Future.successful(Ok(Seq(", 1)
      refServices.foreach { r =>
        val comma = if (refServices.lastOption.contains(r)) { "" } else { "," }
        file.add(s"""RelationCount(model = "${r._2.propertyName}", field = "${r._3.propertyName}", count = ${r._2.propertyName}By${r._3.className})$comma""")
      }
      file.add(").asJson.spaces2).as(JSON))", -1)
      file.add("}", -1)
    }
  }

  def refServiceArgs(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val refServices = model.validReferences(config).map(_.srcTable).distinct.map(config.getModel)
    refServices.foreach(s => file.addImport(s.servicePackage.mkString("."), s.className + "Service"))
    refServices.map(s => s.propertyName + "S: " + s.className + "Service").mkString(", ")
  }
}
