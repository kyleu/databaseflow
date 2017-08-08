package models.scalaexport

import services.scalaexport.ExportTable
import services.scalaexport.config.ExportConfig

case class ExportResult(id: String, tables: Seq[ExportTable], files: Seq[OutputFile], config: ExportConfig.Result) {
  private[this] val startTime = System.currentTimeMillis
  private[this] val logs = collection.mutable.ArrayBuffer.empty[(Int, String)]

  val models = tables.map(t => t.pkg -> t.className)

  def getExportTable(id: String) = tables.find(t => t.propertyName == id || t.t.name == id).getOrElse(throw new IllegalStateException(s"Missing table [$id]."))

  def log(msg: String) = logs += ((System.currentTimeMillis - startTime).toInt -> msg)
  val getLogs: Seq[(Int, String)] = logs

  def getMarkers(key: String) = files.flatMap(_.markersFor(key)).distinct
}
