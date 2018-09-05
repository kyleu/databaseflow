package com.databaseflow.models.scalaexport.db

import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.OutputFile

case class ExportResult(
    config: ExportConfiguration,
    models: Seq[ExportModel],
    enumFiles: Seq[OutputFile],
    sourceFiles: Seq[OutputFile],
    rootFiles: Seq[OutputFile],
    docFiles: Seq[OutputFile]
) {
  private[this] val startTime = System.currentTimeMillis
  private[this] val logs = collection.mutable.ArrayBuffer.empty[(Int, String)]

  def getModel(id: String) = models.find(t => t.propertyName == id || t.tableName == id).getOrElse(throw new IllegalStateException(s"Missing table [$id]."))

  def log(msg: String) = logs += ((System.currentTimeMillis - startTime).toInt -> msg)
  val getLogs: Seq[(Int, String)] = logs

  def getMarkers(key: String) = sourceFiles.flatMap(_.markersFor(key)).distinct

  val fileCount = enumFiles.size + sourceFiles.size + rootFiles.size + docFiles.size
  lazy val fileSizes = Seq(enumFiles, sourceFiles, rootFiles, docFiles).flatten.map(_.rendered.length).sum
}
