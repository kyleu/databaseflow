package services.scalaexport

import models.scalaexport.{ExportResult, ScalaFile}
import models.schema.{Column, Schema, Table}
import utils.FutureUtils.defaultContext

import scala.concurrent.Future

object ScalaExportService {
  def test(schema: Schema) = {
    ScalaExportFiles.reset()
    export(schema).map { result =>
      ScalaExportFiles.persist(result)
      result
    }
  }

  def export(schema: Schema) = {
    val id = schema.catalog.orElse(schema.schemaName).getOrElse(schema.username)
    val tableResults = schema.tables.flatMap(exportTable)
    val files = Seq("index.scala" -> "Hello, world!") ++ tableResults
    Future.successful(ExportResult(id, files))
  }

  private[this] def exportTable(t: Table) = {
    val className = ScalaExportHelper.toScalaClassName.convert(t.name)
    val cls = exportClass(className, t.columns)
    val queries = exportQueries(className, t)
    Seq(cls, queries)
  }

  private[this] def toProp(col: Column) = {
    val propName = ScalaExportHelper.toScalaIdentifier.convert(col.name)
    s"""$propName: ${col.columnType.asScala} = "bar""""
  }

  private[this] def exportClass(className: String, columns: Seq[Column]) = {
    val file = ScalaFile("models", className)
    file.add(s"case class $className(", 1)
    columns.map { col =>
      val comma = if (columns.lastOption.contains(col)) { "" } else { "," }
      file.add(toProp(col) + comma)
    }
    file.add(")", -1)
    file.filename -> file.render()
  }

  private[this] def exportQueries(className: String, t: Table) = {
    val file = ScalaFile("models.queries", className + "Queries")
    file.add(s"object ${className}Queries extends BaseQueries[$className] {", 1)
    file.add("override val columns = Seq(" + t.columns.map("\"" + _.name + "\"").mkString(", ") + ")")
    file.add("}", -1)
    file.filename -> file.render()
  }
}
