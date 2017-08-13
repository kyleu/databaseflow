package services.scalaexport

import better.files._
import models.scalaexport.ExportResult
import services.scalaexport.config.ExportConfiguration

import scala.concurrent.{ExecutionContext, Future}

case class ScalaExportService(config: ExportConfiguration) {
  def test(persist: Boolean = false)(implicit ec: ExecutionContext) = {
    export(config).map { result =>
      val injected = if (persist) {
        ExportFiles.persist(result)

        val rootDir = config.projectLocation match {
          case Some(l) => l.toFile
          case None => s"./tmp/${result.config.key}".toFile
        }

        ExportMerge.merge(result, rootDir)
        ExportInject.inject(result, rootDir)
      } else {
        result.log("Test run completed.")
        Nil
      }
      result -> injected
    }
  }

  def export(config: ExportConfiguration) = {
    val models = config.models.map(model => ExportFiles.exportModel(config, model))
    Future.successful(ExportResult(config, models.map(_._1), models.flatMap(t => t._2)))
  }
}

