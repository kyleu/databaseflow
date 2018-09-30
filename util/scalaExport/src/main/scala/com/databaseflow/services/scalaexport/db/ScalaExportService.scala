package com.databaseflow.services.scalaexport.db

import com.databaseflow.models.scalaexport.db.ExportResult
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.services.scalaexport.ExportFiles
import com.databaseflow.services.scalaexport.db.file.{RoutesFiles, ServiceRegistryFiles, WikiFiles}

import scala.concurrent.{ExecutionContext, Future}

object ScalaExportService {
  case class Result(er: ExportResult, files: Map[String, Int], out: Seq[(String, String)])
}

case class ScalaExportService(config: ExportConfiguration) {
  def export(persist: Boolean = false)(implicit ec: ExecutionContext) = exportFiles().map { result =>
    val injected = if (persist) {
      ExportFiles.persist(result, ExportFiles.prepareRoot())

      val mergeResults = ExportMerge.mergeDirectories(
        projectId = Some(result.config.projectId),
        projectTitle = result.config.projectTitle,
        coreDir = config.coreDir,
        root = config.rootDir -> result.rootFiles,
        wiki = config.wikiDir -> result.docFiles,
        log = result.log,
        source = result.config.source
      )
      mergeResults -> ExportInject.inject(result, config.rootDir)
    } else {
      result.log("Test run completed.")
      Map.empty[String, Int] -> Nil
    }
    ScalaExportService.Result(result, injected._1, injected._2)
  }

  private[this] def exportFiles() = {
    val enumFiles = ExportFiles.exportEnums(config)
    val models = config.models.filterNot(_.ignored)
    val views = config.views.filterNot(_.ignored)
    val modelFiles = models.map(model => ExportFiles.exportModel(config, model))
    val viewFiles = if (config.exportViews) { views.map(view => ExportFiles.exportModel(config, view)) } else { Nil }
    val rootFiles = RoutesFiles.files(config, models) ++ ServiceRegistryFiles.files(models, config.pkgPrefix)
    val docFiles = WikiFiles.export(config, models)
    Future.successful(ExportResult(
      config = config,
      models = modelFiles.map(_._1),
      views = viewFiles.map(_._1),
      enumFiles = enumFiles,
      sourceFiles = modelFiles.flatMap(_._2) ++ viewFiles.flatMap(_._2),
      rootFiles = rootFiles,
      docFiles = docFiles
    ))
  }
}
