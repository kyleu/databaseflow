package services.scalaexport

import models.scalaexport.ExportResult
import models.schema.Schema

import scala.concurrent.{ExecutionContext, Future}

case class ScalaExportService(schema: Schema) {
  val id = ExportHelper.toIdentifier(schema.catalog.orElse(schema.schemaName).getOrElse(schema.username))

  def test(persist: Boolean = false)(implicit ec: ExecutionContext) = {
    export(id, schema).map { result =>
      if (persist) {
        ExportFiles.persist(result)
        ExportMerge.merge(result)
        ExportInject.inject(result)
      }
      result
    }
  }

  private[this] val config = ExportConfig.load(id)

  def export(projectId: String, schema: Schema) = {
    val tables = schema.tables.map(t => ExportFiles.exportTable(schema, ExportTable(t, config, schema)))

    val models = tables.map(t => t._1.pkg -> t._1.className)
    val files = tables.flatMap(t => t._2)
    Future.successful(ExportResult(projectId, models, files))
  }
}
