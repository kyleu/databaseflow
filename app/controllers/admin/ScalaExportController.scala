package controllers.admin

import better.files._
import controllers.BaseController
import models.schema.Schema
import services.connection.ConnectionSettingsService
import services.scalaexport.{ExportHelper, ScalaExportService}
import services.scalaexport.config._
import services.schema.SchemaService
import upickle.Js
import util.ApplicationContext
import util.FutureUtils.defaultContext
import util.web.FormUtils
import upickle.default._

import scala.concurrent.Future

object ScalaExportController {
  def readConfiguration(json: Js.Value) = readJs[ExportConfiguration](json)
  def writeConfiguration(c: ExportConfiguration) = writeJs[ExportConfiguration](c)
}

@javax.inject.Singleton
class ScalaExportController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def exportThriftForm(conn: String) = withAdminSession("export.form") { implicit request =>
    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => Future.successful(Ok(views.html.admin.scalaExport.thriftForm(request.identity, cs)))
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }

  def exportThrift(conn: String) = withAdminSession("export.project") { implicit request =>
    val form = FormUtils.getForm(request)
    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => SchemaService.getSchemaWithDetails(cs).map { schema =>
        val config = getConfig(schema)
        val result = ScalaExportService(config).exportThrift(key = config.projectId, filename = form("filename"), persist = true)
        Ok(views.html.admin.scalaExport.exportThrift(request.identity, result._1, result._2))
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }

  def exportForm(conn: String) = withAdminSession("export.form") { implicit request =>
    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => SchemaService.getSchemaWithDetails(cs).map { schema =>
        Ok(views.html.admin.scalaExport.schemaForm(request.identity, cs, getConfig(schema), schema))
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
          ScalaExportHelper.enumFor(e, form.filter(_._1.startsWith("enum." + e.key)).map(x => x._1.stripPrefix("enum." + e.key + ".") -> x._2))
        }
        val config = ExportConfiguration(
          key = schemaId,
          projectId = form("project.id"),
          projectTitle = form("project.title"),
          enums = enums,
          models = schema.tables.map { t =>
            val prefix = s"model.${t.name}."
            ScalaExportHelper.modelForTable(schema, t, form.filter(_._1.startsWith(prefix)).map(x => x._1.stripPrefix(prefix) -> x._2), enums)
          },
          source = form("project.source"),
          projectLocation = form.get("project.location").filter(_.nonEmpty)
        )

        val x = write(config, indent = 2)
        s"./tmp/$schemaId.txt".toFile.overwrite(x)

        ScalaExportService(config).export(persist = true).map { result =>
          Ok(views.html.admin.scalaExport.export(result.er, result.files, result.out))
        }
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }

  private[this] def getConfig(schema: Schema) = {
    val schemaId = ExportHelper.toIdentifier(schema.catalog.orElse(schema.schemaName).getOrElse(schema.username))
    val f = s"./tmp/$schemaId.txt".toFile
    if (f.exists) {
      ScalaExportHelper.merge(schema, read[ExportConfiguration](f.contentAsString))
    } else {
      ExportConfigurationDefault.forSchema(schemaId, schema)
    }
  }
}
