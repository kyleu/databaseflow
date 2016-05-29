package controllers

import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ Action, Controller }
import services.FeedbackService
import services.FeedbackService.Feedback

import scala.concurrent.Future

object FeedbackController {
  val feedbackForm = Form(
    mapping(
      "id" -> uuid,
      "email" -> email,
      "content" -> nonEmptyText
    )(Feedback.apply)(Feedback.unapply)
  )
}

@javax.inject.Singleton
class FeedbackController @javax.inject.Inject() (implicit val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def feedbackForm() = Action.async { implicit request =>
    Future.successful(Ok(views.html.feedbackForm()))
  }

  def postFeedback() = Action.async { implicit request =>
    val action = FeedbackController.feedbackForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.feedbackForm()),
      feedback => {
        FeedbackService.save(feedback)
        Redirect(controllers.routes.HomeController.index()).flashing("success" -> "Thanks for your feedback!")
      }
    )

    Future.successful(action)
  }
}
