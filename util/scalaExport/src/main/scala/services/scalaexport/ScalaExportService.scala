package services.scalaexport

import better.files._
import models.scalaexport.ExportResult
import services.scalaexport.config.ExportConfiguration
import services.scalaexport.file.db.{RoutesFiles, ServiceRegistryFiles}
import services.scalaexport.thrift.ThriftParseService

import scala.concurrent.{ExecutionContext, Future}

object ScalaExportService {
  case class Result(er: ExportResult, files: Map[String, Int], out: Seq[(String, String)])
}

case class ScalaExportService(config: ExportConfiguration) {
  def export(persist: Boolean = false)(implicit ec: ExecutionContext) = exportFiles().map { result =>
    val injected = if (persist) {
      ExportFiles.persist(result)

      val rootDir = config.projectLocation match {
        case Some(l) => l.toFile
        case None => s"./tmp/${result.config.key}".toFile
      }

      val mergeResults = ExportMerge.merge(result.config.projectId, result.config.projectTitle, rootDir, result.rootFiles, result.log, result.config.source)
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
    val rootFiles = RoutesFiles.files(config, models) ++ ServiceRegistryFiles.files(models)
    Future.successful(ExportResult(config, modelFiles.map(_._1), enumFiles, modelFiles.flatMap(_._2), rootFiles))
  }

  def exportThrift(key: String, filename: String, persist: Boolean = false)(implicit ec: ExecutionContext): (Map[String, Int], Seq[(String, String)]) = {
    if (filename == "all") {
      val root = File("./tmp/thrift")
      if (!root.exists) {
        throw new IllegalStateException(s"Cannot read [${root.pathAsString}].")
      }
      val results = root.children.flatMap {
        case f if f.isDirectory => f.children.flatMap {
          case c if c.name.endsWith(".thrift") => Some(exportThrift(key, c.pathAsString, persist))
          case _ => None
        }
        case f if f.name.endsWith(".thrift") => Seq(exportThrift(key, f.pathAsString, persist))
        case _ => Nil
      }.toSeq

      val map = results.map(_._1).foldLeft(Map.empty[String, Int])((l, r) => l ++ r)
      val seq = results.map(_._2).foldLeft(Seq.empty[(String, String)])((l, r) => l ++ r)
      map -> seq
    } else {
      val result = ThriftParseService.parse(better.files.File(filename))
      val injected = if (persist) {
        ExportFiles.persistThrift(result)

        val rootDir = config.projectLocation match {
          case Some(l) => l.toFile
          case None => s"./tmp/$key".toFile
        }

        ExportMerge.merge(key, "N/A", rootDir, Nil, s => println(s)) -> result.allFiles.map(f => f.path -> f.rendered)
      } else {
        Map.empty[String, Int] -> Nil
      }
      injected
    }
  }
}
