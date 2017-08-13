package controllers.admin

import com.ibm.db2.jcc.am.id
import controllers.BaseController
import models.schema.{Column, ColumnType, Table}
import services.connection.ConnectionSettingsService
import services.scalaexport.config._
import services.scalaexport.{ExportHelper, ScalaExportService}
import services.schema.SchemaService
import upickle.Js
import util.ApplicationContext
import util.FutureUtils.defaultContext
import util.web.FormUtils

import scala.concurrent.Future

object ScalaExportController {
  import upickle.default._

  def readConfiguration(json: Js.Value) = readJs[ExportConfiguration](json)
  def writeConfiguration(c: ExportConfiguration) = writeJs[ExportConfiguration](c)
}

@javax.inject.Singleton
class ScalaExportController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def exportFormNew(conn: String) = withAdminSession("export.form") { implicit request =>
    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => SchemaService.getSchemaWithDetails(cs).map { schema =>
        val schemaId = ExportHelper.toIdentifier(schema.catalog.orElse(schema.schemaName).getOrElse(schema.username))
        val config = ExportConfigurationDefault.forSchema(schemaId, schema)
        Ok(views.html.admin.scalaExport.schemaForm(request.identity, cs, config))
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }

  def exportForm(conn: String) = withAdminSession("export.form") { implicit request =>
    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => SchemaService.getSchemaWithDetails(cs).map { schema =>
        val schemaId = ExportHelper.toIdentifier(schema.catalog.orElse(schema.schemaName).getOrElse(schema.username))
        val config = ExportConfigReader.read(schemaId)
        Ok(views.html.admin.scalaExport.exportForm(request.identity, cs, schema, config))
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
          key = conn,
          projectId = form("project.id"),
          projectTitle = form("project.title"),
          models = schema.tables.map { t =>
            modelForTable(t, form.filter(_._1.startsWith("model." + t.name)).map(x => x._1.stripPrefix("model." + t.name + ".") -> x._2))
          },
          engine = form("engine"),
          projectLocation = form.get("project.location").filter(_.nonEmpty)
        )
        //ExportConfigWriter.write(result)

        // ScalaExportService(schema).test(persist = true).map { result =>
        //   Ok(views.html.admin.scalaExport.export(result._1, result._2))
        // }
        Future.successful(Ok(play.twirl.api.Html("OK, for now...")))
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }

  private[this] def modelForTable(t: Table, form: Map[String, String]) = {
    ExportConfiguration.Model(
      tableName = t.name,
      pkg = form("pkg").split('.').filter(_.nonEmpty),
      propertyName = form("propertyName"),
      className = form("className"),
      title = form("title"),
      plural = form("plural"),
      fields = t.columns.map { col =>
        fieldForColumn(col, form.filter(_._1.startsWith("field.")).map(x => x._1.stripPrefix("field.") -> x._2))
      },
      extendsClass = form.get("extendsClass").filter(_.nonEmpty),
      ignored = form.get("ignored").contains("true"),
      provided = form.get("provided").contains("true")
    )
  }

  private[this] def fieldForColumn(col: Column, form: Map[String, String]) = {
    ExportConfiguration.Model.Field(
      columnName = col.name,
      propertyName = form(col.name + ".propertyName"),
      title = form(col.name + ".title"),
      t = ColumnType.withNameInsensitive(form(col.name + ".t")),
      inSearch = form.get(col.name + ".inSearch").contains("true"),
      inView = form.get(col.name + ".inView").contains("true"),
      ignored = form.get(col.name + ".ignored").contains("true")
    )
  }

  def exportOld(conn: String) = withAdminSession("export.project") { implicit request =>
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
