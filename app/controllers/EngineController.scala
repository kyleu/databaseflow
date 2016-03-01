package controllers

import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class EngineController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def list() = act("list") { implicit request =>
    Future.successful(Ok(models.engine.DatabaseEngine.all.mkString(", ")))
  }

  def detail(key: String) = act(key) { implicit request =>
    Future.successful(Ok("..."))
  }
}
