package controllers

import java.util.UUID

import play.api.Mode
import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class QueryController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def main(connectionId: UUID) = withSession(s"connection-$connectionId") { implicit request =>
    val debug = ctx.playEnv.mode == Mode.Dev
    Future.successful(Ok(views.html.query.main(request.identity, debug)))
  }
}
