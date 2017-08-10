package services.scalaexport

import better.files._
import models.scalaexport.ExportResult
import models.schema.Schema
import services.scalaexport.config.ExportConfigReader

import scala.concurrent.{ExecutionContext, Future}

case class ScalaExportService(schema: Schema) {
  private[this] val schemaId = ExportHelper.toIdentifier(schema.catalog.orElse(schema.schemaName).getOrElse(schema.username))
  private[this] val config = ExportConfigReader.read(schemaId)

  def test(persist: Boolean = false)(implicit ec: ExecutionContext) = {
    export(config.projectName, schema).map { result =>
      val injected = if (persist) {
        ExportFiles.persist(result)

        val rootDir = config.projectLocation match {
          case Some(l) => l.toFile
          case None => s"./tmp/${ExportHelper.toIdentifier(result.id)}".toFile
        }

        ExportMerge.merge(result, rootDir)
        ExportInject.inject(result, rootDir)
      } else {
        Nil
      }
      result -> injected
    }
  }

  def export(projectId: String, schema: Schema) = {
    val exportTables = schema.tables.map(t => ExportTable(t, config, schema))
    val tables = exportTables.map(t => ExportFiles.exportTable(schema, t))

    Future.successful(ExportResult(projectId, tables.map(_._1), tables.flatMap(t => t._2), config))
  }
}
