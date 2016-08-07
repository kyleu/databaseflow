package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.{Action, Controller}
import services.translation.TranslationService

@javax.inject.Singleton
class TranslationController @javax.inject.Inject() (translationService: TranslationService) extends Controller {
  def index = Action.async {
    val root = new java.io.File("./conf")
    val keys = translationService.translateAll("yandex", root)
    keys._2.map { v =>
      Ok(views.html.index(v.mkString(", ")))
    }
  }
}
