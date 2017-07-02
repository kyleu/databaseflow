package services.scalaexport

import models.scalaexport.ExportResult
import models.schema.{Schema, Table}
import services.scalaexport.file.{ClassFile, QueriesFile, ServiceFile}

import scala.concurrent.{ExecutionContext, Future}

object ScalaExportService {
  def test(schema: Schema)(implicit ec: ExecutionContext) = {
    val id = schema.catalog.orElse(schema.schemaName).getOrElse(schema.username)
    ExportFiles.reset(id)
    export(id, schema).map { result =>
      ExportFiles.persist(result)
      result
    }
  }

  def export(projectId: String, schema: Schema) = {
    val tableResults = schema.tables.flatMap(exportTable)
    val files = Seq("index.scala" -> "Hello, world!") ++ tableResults
    Future.successful(ExportResult(projectId, files))
  }

  private[this] def exportTable(t: Table) = {
    val className = ExportHelper.toScalaClassName.convert(t.name)
    val cls = ClassFile.export(className, t.columns)
    val queries = QueriesFile.export(className, t)
    val svc = ServiceFile.export(className, t)
    Seq(cls, queries, svc)
  }
}
