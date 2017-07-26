package services.scalaexport

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
        ExportMerge.merge(result)
        ExportInject.inject(result)
      } else {
        Nil
      }
      result -> injected
    }
  }

  def export(projectId: String, schema: Schema) = {
    val exportTables = schema.tables.map(t => ExportTable(t, config, schema))
    val tables = exportTables.map(t => ExportFiles.exportTable(schema, t, config))

    val models = tables.map(t => t._1.pkg -> t._1.className)
    val files = tables.flatMap(t => t._2)
    Future.successful(ExportResult(projectId, models, files))
  }
}
