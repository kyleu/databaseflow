package services.scalaexport

import better.files._
import models.scalaexport.ExportResult
import services.scalaexport.config.ExportConfiguration
import services.scalaexport.file.{RoutesFiles, ServiceRegistryFiles}

import scala.concurrent.{ExecutionContext, Future}

case class ScalaExportService(config: ExportConfiguration) {
  def export(persist: Boolean = false)(implicit ec: ExecutionContext) = exportFiles(config).map { result =>
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

  def exportFiles(config: ExportConfiguration) = {
    val models = config.models.filterNot(_.ignored)
    val modelFiles = models.map(model => ExportFiles.exportModel(config, model))
    val rootFiles = RoutesFiles.files(config, models) ++ ServiceRegistryFiles.files(models)
    Future.successful(ExportResult(config, modelFiles.map(_._1), modelFiles.flatMap(_._2), rootFiles))
  }
}

