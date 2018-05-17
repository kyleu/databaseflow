package controllers.schema

import controllers.BaseController
import services.connection.ConnectionSettingsService
import services.schema.{MermaidChartService, SchemaService}
import util.ApplicationContext

import util.FutureUtils.defaultContext

@javax.inject.Singleton
class SchemaController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def chart(id: String) = withSession("detail." + id) { implicit request =>
    val conn = ConnectionSettingsService.connFor(id).getOrElse(throw new IllegalStateException(s"Invalid connection [$id]"))
    SchemaService.getSchemaWithDetails(Some(request.identity), conn).map { schema =>
      val chartData = MermaidChartService.chartFor(schema)
      Ok(views.html.schema.mermaid(request.identity, id, conn.name, chartData))
    }
  }
}
