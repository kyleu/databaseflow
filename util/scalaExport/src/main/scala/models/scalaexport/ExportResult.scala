package models.scalaexport

import services.scalaexport.config.{ExportConfiguration, ExportModel}

case class ExportResult(config: ExportConfiguration, models: Seq[ExportModel], sourceFiles: Seq[OutputFile], rootFiles: Seq[OutputFile]) {
  private[this] val startTime = System.currentTimeMillis
  private[this] val logs = collection.mutable.ArrayBuffer.empty[(Int, String)]

  def getModel(id: String) = models.find(t => t.propertyName == id || t.tableName == id).getOrElse(throw new IllegalStateException(s"Missing table [$id]."))

  def log(msg: String) = logs += ((System.currentTimeMillis - startTime).toInt -> msg)
  val getLogs: Seq[(Int, String)] = logs

  def getMarkers(key: String) = sourceFiles.flatMap(_.markersFor(key)).distinct

  val fileCount = sourceFiles.size + rootFiles.size
  lazy val fileSizes = sourceFiles.map(_.rendered.length).sum + rootFiles.map(_.rendered.length).sum
}
