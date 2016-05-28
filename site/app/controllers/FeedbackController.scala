package controllers

import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, Controller }

import scala.concurrent.Future

@javax.inject.Singleton
class FeedbackController @javax.inject.Inject() (implicit val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def feedbackForm() = Action.async { implicit request =>
    Future.successful(Ok(views.html.feedbackForm()))
  }

  def postFeedback() = Action.async { implicit request =>
    Future.successful(Redirect(controllers.routes.HomeController.index()).flashing("success" -> "Thanks for your feedback!"))
  }
}
