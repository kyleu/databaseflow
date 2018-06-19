package com.databaseflow.services.scalaexport.db

import better.files._
import com.databaseflow.models.scalaexport.db
import com.databaseflow.models.scalaexport.db.ExportResult
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.services.scalaexport.ExportFiles
import com.databaseflow.services.scalaexport.db.file.{RoutesFiles, ServiceRegistryFiles}

import scala.concurrent.{ExecutionContext, Future}

object ScalaExportService {
  case class Result(er: ExportResult, files: Map[String, Int], out: Seq[(String, String)])
}

case class ScalaExportService(config: ExportConfiguration) {
  def export(persist: Boolean = false)(implicit ec: ExecutionContext) = exportFiles().map { result =>
    val injected = if (persist) {
      ExportFiles.persist(result, ExportFiles.prepareRoot())

      val rootDir = config.projectLocation match {
        case Some(l) => l.toFile
        case None => s"./tmp/${result.config.key}".toFile
      }

      val mergeResults = ExportMerge.merge(
        projectId = Some(result.config.projectId),
        projectTitle = result.config.projectTitle,
        rootDir = rootDir,
        rootFiles = result.rootFiles,
        log = result.log,
        source = result.config.source
      )
      mergeResults -> ExportInject.inject(result, rootDir)
    } else {
      result.log("Test run completed.")
      Map.empty[String, Int] -> Nil
    }
    ScalaExportService.Result(result, injected._1, injected._2)
  }

  private[this] def exportFiles() = {
    val enumFiles = ExportFiles.exportEnums(config)
    val models = config.models.filterNot(_.ignored)
    val modelFiles = models.map(model => ExportFiles.exportModel(config, model))
    val rootFiles = RoutesFiles.files(config, models) ++ ServiceRegistryFiles.files(models, config.pkgPrefix)
    Future.successful(db.ExportResult(config, modelFiles.map(_._1), enumFiles, modelFiles.flatMap(_._2), rootFiles))
  }
}
