package controllers.admin

import better.files._
import controllers.BaseController
import models.schema._
import services.connection.ConnectionSettingsService
import services.scalaexport.{ExportHelper, ScalaExportService}
import services.scalaexport.config._
import services.schema.SchemaService
import upickle.Js
import util.ApplicationContext
import util.FutureUtils.defaultContext
import util.web.FormUtils
import upickle.default._

import scala.util.control.NonFatal

object ScalaExportController {
  def readConfiguration(json: Js.Value) = readJs[ExportConfiguration](json)
  def writeConfiguration(c: ExportConfiguration) = writeJs[ExportConfiguration](c)
}

@javax.inject.Singleton
class ScalaExportController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {

  private[this] def merge(schema: Schema, config: ExportConfiguration) = {
    val models = schema.tables.map { t =>
      config.getModelOpt(t.name) match {
        case Some(m) =>
          val fields = t.columns.map { c =>
            m.fields.find(_.columnName == c.name).getOrElse(ExportConfigurationDefault.loadField(c, enums = config.enums))
          }
          m.copy(fields = fields.toList)
        case None => ExportConfigurationDefault.loadModel(schema, t, config.enums)
      }
    }
    config.copy(models = models)
  }

  def exportForm(conn: String) = withAdminSession("export.form") { implicit request =>
    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => SchemaService.getSchemaWithDetails(cs).map { schema =>
        val schemaId = ExportHelper.toIdentifier(schema.catalog.orElse(schema.schemaName).getOrElse(schema.username))
        val f = s"./tmp/$schemaId.txt".toFile
        val config = if (f.exists) {
          merge(schema, read[ExportConfiguration](f.contentAsString))
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
        val enums = schema.enums.map { e =>
          enumFor(e, form.filter(_._1.startsWith("enum." + e.key)).map(x => x._1.stripPrefix("enum." + e.key + ".") -> x._2))
        }
        val result = ExportConfiguration(
          key = schemaId,
          projectId = form("project.id"),
          projectTitle = form("project.title"),
          enums = enums,
          models = schema.tables.map { t =>
            val prefix = s"model.${t.name}."
            modelForTable(schema, t, form.filter(_._1.startsWith(prefix)).map(x => x._1.stripPrefix(prefix) -> x._2), enums)
          },
          source = form("project.source"),
          projectLocation = form.get("project.location").filter(_.nonEmpty)
        )

        val x = write(result, indent = 2)
        s"./tmp/$schemaId.txt".toFile.overwrite(x)

        ScalaExportService(result).export(persist = true).map { result =>
          Ok(views.html.admin.scalaExport.export(result._1, result._2))
        }
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }

  private[this] def enumFor(e: EnumType, form: Map[String, String]) = try {
    ExportEnum(
      pkg = form("pkg").split('.').filter(_.nonEmpty).toList,
      name = e.key,
      className = form("className"),
      values = e.values,
      ignored = form.get("ignored").contains("true")
    )
  } catch {
    case NonFatal(x) => throw new IllegalStateException(s"Unable to create model for enum [${e.key}].", x)
  }

  private[this] def modelForTable(schema: Schema, t: Table, form: Map[String, String], enums: Seq[ExportEnum]) = try {
    ExportModel(
      tableName = t.name,
      pkg = form("pkg").split('.').filter(_.nonEmpty).toList,
      propertyName = form("propertyName"),
      className = form("className"),
      title = form("title"),
      description = form.get("description").filter(_.nonEmpty),
      plural = form("plural"),
      fields = t.columns.map(col => fieldForColumn(col, form.filter(_._1.startsWith("field.")).map(x => x._1.stripPrefix("field.") -> x._2), enums)).toList,
      pkColumns = ExportConfigurationHelper.pkColumns(schema, t),
      foreignKeys = t.foreignKeys.toList,
      references = ExportConfigurationHelper.references(schema, t),
      extendsClass = form.get("extendsClass").filter(_.nonEmpty),
      icon = form.get("icon").filter(_.nonEmpty),
      scalaJs = form.get("scalaJs").contains("true"),
      ignored = form.get("ignored").contains("true"),
      audited = form.get("audited").contains("true"),
      provided = form.get("provided").contains("true")
    )
  } catch {
    case NonFatal(x) => throw new IllegalStateException(s"Unable to create model for table [${t.name}].", x)
  }

  private[this] def fieldForColumn(col: Column, form: Map[String, String], enums: Seq[ExportEnum]) = {
    ExportField(
      columnName = col.name,
      propertyName = form(col.name + ".propertyName"),
      title = form(col.name + ".title"),
      description = form.get(col.name + ".description").filter(_.nonEmpty),
      t = enums.find(_.name == col.sqlTypeName).map(_ => ColumnType.EnumType).getOrElse(ColumnType.withNameInsensitive(form(col.name + ".t"))),
      sqlTypeName = col.sqlTypeName,
      enumOpt = enums.find(_.name == col.sqlTypeName),
      defaultValue = form.get(col.name + ".defaultValue").orElse(col.defaultValue),
      notNull = form.get(col.name + ".notNull").map(_ == "true").getOrElse(col.notNull),
      inSearch = form.get(col.name + ".inSearch").contains("true"),
      inView = form.get(col.name + ".inView").contains("true"),
      inSummary = form.get(col.name + ".inSummary").contains("true"),
      ignored = form.get(col.name + ".ignored").contains("true")
    )
  }
}
