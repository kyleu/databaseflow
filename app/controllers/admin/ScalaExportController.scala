package controllers.admin

import better.files._
import controllers.BaseController
import models.schema.{Column, ColumnType, Schema, Table}
import services.connection.ConnectionSettingsService
import services.scalaexport.{ExportHelper, ScalaExportService}
import services.scalaexport.config._
import services.schema.SchemaService
import upickle.Js
import util.ApplicationContext
import util.FutureUtils.defaultContext
import util.web.FormUtils
import upickle.default._

object ScalaExportController {
  def readConfiguration(json: Js.Value) = readJs[ExportConfiguration](json)
  def writeConfiguration(c: ExportConfiguration) = writeJs[ExportConfiguration](c)
}

@javax.inject.Singleton
class ScalaExportController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def exportForm(conn: String) = withAdminSession("export.form") { implicit request =>
    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => SchemaService.getSchemaWithDetails(cs).map { schema =>
        val schemaId = ExportHelper.toIdentifier(schema.catalog.orElse(schema.schemaName).getOrElse(schema.username))
        val f = s"./tmp/$schemaId.txt".toFile
        val config = if (f.exists) {
          read[ExportConfiguration](f.contentAsString)
        } else {
          ExportConfigurationDefault.forSchema(schemaId, schema)
        }
        Ok(views.html.admin.scalaExport.schemaForm(request.identity, cs, config, schema))
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }

  def export(conn: String) = withAdminSession("export.project") { implicit request =>
    val form = FormUtils.getForm(request)

    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => SchemaService.getSchemaWithDetails(cs).flatMap { schema =>
        val schemaId = ExportHelper.toIdentifier(schema.catalog.orElse(schema.schemaName).getOrElse(schema.username))
        val result = ExportConfiguration(
          key = schemaId,
          projectId = form("project.id"),
          projectTitle = form("project.title"),
          models = schema.tables.map { t =>
            modelForTable(schema, t, form.filter(_._1.startsWith("model." + t.name)).map(x => x._1.stripPrefix("model." + t.name + ".") -> x._2))
          },
          engine = ExportEngine.withNameInsensitive(form("engine")),
          projectLocation = form.get("project.location").filter(_.nonEmpty)
        )

        val x = write(result, indent = 2)
        s"./tmp/$schemaId.txt".toFile.overwrite(x)

        ScalaExportService(result).test(persist = true).map { result =>
          Ok(views.html.admin.scalaExport.export(result._1, result._2))
        }
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }

  private[this] def modelForTable(schema: Schema, t: Table, form: Map[String, String]) = ExportModel(
    tableName = t.name,
    pkg = form("pkg").split('.').filter(_.nonEmpty).toList,
    propertyName = form("propertyName"),
    className = form("className"),
    title = form("title"),
    description = form.get("description").filter(_.nonEmpty),
    plural = form("plural"),
    fields = t.columns.map(col => fieldForColumn(col, form.filter(_._1.startsWith("field.")).map(x => x._1.stripPrefix("field.") -> x._2))).toList,
    pkColumns = ExportConfigurationHelper.pkColumns(schema, t),
    foreignKeys = t.foreignKeys.toList,
    references = ExportConfigurationHelper.references(schema, t),
    extendsClass = form.get("extendsClass").filter(_.nonEmpty),
    ignored = form.get("ignored").contains("true"),
    provided = form.get("provided").contains("true")
  )

  private[this] def fieldForColumn(col: Column, form: Map[String, String]) = ExportField(
    columnName = col.name,
    propertyName = form(col.name + ".propertyName"),
    title = form(col.name + ".title"),
    description = form.get(col.name + ".description").filter(_.nonEmpty),
    t = ColumnType.withNameInsensitive(form(col.name + ".t")),
    sqlTypeName = col.sqlTypeName,
    defaultValue = form.get(col.name + ".defaultValue").orElse(col.defaultValue),
    notNull = form.get(col.name + ".notNull").map(_ == "true").getOrElse(col.notNull),
    inSearch = form.get(col.name + ".inSearch").contains("true"),
    inView = form.get(col.name + ".inView").contains("true"),
    ignored = form.get(col.name + ".ignored").contains("true")
  )
}
