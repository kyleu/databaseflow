package services.scalaexport

import models.scalaexport.ExportResult
import models.schema.{Schema, Table}
import services.scalaexport.file.{ClassFile, QueriesFile, ServiceFile}

import scala.concurrent.{ExecutionContext, Future}

case class ScalaExportService(schema: Schema) {
  val id = schema.catalog.orElse(schema.schemaName).getOrElse(schema.username)

  def test(persist: Boolean = false)(implicit ec: ExecutionContext) = {
    export(id, schema).map { result =>
      if (persist) {
        ExportFiles.persist(result)
        ExportFiles.merge(result.id)
      }
      result
    }
  }

  def export(projectId: String, schema: Schema) = {
    val tableResults = schema.tables.flatMap(exportTable)
    val files = tableResults
    Future.successful(ExportResult(projectId, files))
  }

  private[this] val (classNameSubstitutions, packages) = ExportConfig.load(id)

  private[this] def exportTable(t: Table) = {
    val asClassName = ExportHelper.toScalaClassName.convert(t.name)
    val className = classNameSubstitutions.getOrElse(asClassName, asClassName)
    val pkg = packages.get(t.name).map(x => x.split("\\.").toList).getOrElse(Nil)
    val cls = ClassFile.export(className, pkg, t.columns)
    val queries = QueriesFile.export(className, pkg, t)
    val svc = ServiceFile.export(className, pkg, t)
    Seq(cls, queries, svc)
  }
}
