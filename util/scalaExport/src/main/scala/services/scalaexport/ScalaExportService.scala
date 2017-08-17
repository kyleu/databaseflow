package services.scalaexport

import better.files._
import models.scalaexport.ExportResult
import services.scalaexport.config.ExportConfiguration
import services.scalaexport.file.RoutesFiles

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
    val modelFiles = config.models.map(model => ExportFiles.exportModel(config, model))
    val rootFiles = RoutesFiles.files(config.models)
    Future.successful(ExportResult(config, modelFiles.map(_._1), modelFiles.flatMap(_._2), rootFiles))
  }
}

