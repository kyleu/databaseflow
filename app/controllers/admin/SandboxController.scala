package controllers.admin

import akka.util.Timeout
import controllers.BaseController
import models.sandbox.SandboxTask
import services.connection.ConnectionSettingsService
import services.scalaexport.ScalaExportService
import services.schema.SchemaService
import utils.FutureUtils.defaultContext
import utils.ApplicationContext

import scala.concurrent.Future
import scala.concurrent.duration._

@javax.inject.Singleton
class SandboxController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  implicit val timeout = Timeout(10.seconds)

  def list = withAdminSession("sandbox.list") { implicit request =>
    Future.successful(Ok(views.html.admin.sandbox.list(request.identity)))
  }

  def sandbox(key: String) = withAdminSession("sandbox." + key) { implicit request =>
    val sandbox = SandboxTask.withName(key)
    sandbox.run(ctx).map { result =>
      Ok(views.html.admin.sandbox.run(request.identity, sandbox, result))
    }
  }

  def export(conn: String) = withAdminSession("sandbox.export") { implicit request =>
    ConnectionSettingsService.connFor(conn) match {
      case Some(cs) => SchemaService.getSchemaWithDetails(cs).flatMap { schema =>
        ScalaExportService(schema).test(persist = true).map { result =>
          Ok(views.html.admin.sandbox.export(result._1, result._2))
        }
      }
      case None => throw new IllegalStateException(s"Invalid connection [$conn].")
    }
  }
}
