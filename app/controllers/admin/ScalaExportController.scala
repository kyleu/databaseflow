package controllers.admin

import controllers.BaseController
import services.connection.ConnectionSettingsService
import services.scalaexport.config.{ExportConfigWriter, ExportConfig, ExportConfigReader}
import services.scalaexport.{ExportHelper, ScalaExportService}
import services.schema.SchemaService
import util.FutureUtils.defaultContext
import util.ApplicationContext
import util.web.FormUtils

@javax.inject.Singleton
class ScalaExportController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def exportForm(conn: String) = withAdminSession("sandbox.export") { implicit request =>
    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => SchemaService.getSchemaWithDetails(cs).map { schema =>
        val schemaId = ExportHelper.toIdentifier(schema.catalog.orElse(schema.schemaName).getOrElse(schema.username))
        val config = ExportConfigReader.read(schemaId)
        Ok(views.html.admin.scalaExport.exportForm(cs, schema, config))
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }

  def export(conn: String) = withAdminSession("sandbox.export") { implicit request =>
    val form = FormUtils.getForm(request)

    def formKeyToMap(key: String) = form(key).split('\n').map(_.trim).filter(_.nonEmpty).map { s =>
      s.split('=').map(_.trim).filter(_.nonEmpty).toList match {
        case one :: Nil => one -> "true"
        case one :: two :: Nil => one -> two
        case x => throw new IllegalStateException(s"Invalid line [$x].")
      }
    }.toMap

    val projectName = form("projectName")
    val projectLocation = Some(form("projectLocation")).filter(_.trim.nonEmpty)
    val engine = form("engine")
    val provided = formKeyToMap("provided")
    val classNames = formKeyToMap("classNames")
    val extendModels = formKeyToMap("extendModels")
    val propertyNames = formKeyToMap("propertyNames")
    val packages = formKeyToMap("packages")
    val titles = formKeyToMap("titles").mapValues { v =>
      val idx = v.indexOf(':')
      idx match {
        case -1 => v -> (v + "s")
        case _ => v.substring(0, idx) -> v.substring(idx + 1)
      }
    }
    val searchColumns = formKeyToMap("searchColumns").mapValues(_.split(",").map(_.trim).filter(_.nonEmpty).map { col =>
      val idx = col.indexOf(':')
      if (idx == -1) {
        col -> col
      } else {
        col.substring(0, idx).trim -> col.substring(idx + 1).trim
      }
    }.toSeq)

    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => SchemaService.getSchemaWithDetails(cs).flatMap { schema =>
        val schemaId = ExportHelper.toIdentifier(schema.catalog.orElse(schema.schemaName).getOrElse(schema.username))

        val result = ExportConfig(
          schemaId, projectName, engine, projectLocation, provided, classNames, extendModels, propertyNames, packages, titles, searchColumns
        ).withDefaults
        ExportConfigWriter.write(result)

        ScalaExportService(schema).test(persist = true).map { result =>
          Ok(views.html.admin.scalaExport.export(result._1, result._2))
        }
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }
}
