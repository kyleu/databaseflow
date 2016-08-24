package controllers

import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class SharedResultController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def index() = withoutSession("result.index") { implicit request =>
    Future.successful(Ok(views.html.result.list(request.identity, Nil, ctx.config.debug)))
  }
}
