package models.scalaexport.db.config

import models.scalaexport.db.{ExportEnum, ExportModel}

case class ExportConfiguration(
    key: String,
    projectId: String,
    projectTitle: String,
    enums: Seq[ExportEnum],
    models: Seq[ExportModel],
    source: String = "boilerplay",
    projectLocation: Option[String] = None,
    modelLocationOverride: Option[String] = None,
    thriftLocationOverride: Option[String] = None
) {
  def getModel(k: String) = getModelOpt(k).getOrElse(throw new IllegalStateException(s"No model available with name [$k]."))
  def getModelOpt(k: String) = getModelOptWithIgnored(k).filterNot(_.ignored)
  def getModelOptWithIgnored(k: String) = models.find(m => m.tableName == k || m.propertyName == k || m.className == k)
}
