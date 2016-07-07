package controllers

import org.joda.time.LocalDateTime
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.{Action, Controller}
import services.{EmailService, FeedbackService}
import services.FeedbackService.Feedback

import scala.concurrent.Future

object FeedbackController {
  val feedbackForm = Form(
    mapping(
      "id" -> uuid,
      "email" -> email,
      "content" -> nonEmptyText,
      "created" -> ignored(new LocalDateTime())
    )(Feedback.apply)(Feedback.unapply)
  )
}

@javax.inject.Singleton
class FeedbackController @javax.inject.Inject() (implicit val messagesApi: MessagesApi, emailService: EmailService) extends Controller with I18nSupport {
  def feedbackForm() = Action.async { implicit request =>
    Future.successful(Ok(views.html.feedbackForm()))
  }

  def feedbackOptions() = Action.async { implicit request =>
    Future.successful(Ok("OK").withHeaders(SiteController.cors: _*))
  }

  def postFeedback(ajax: Boolean) = Action.async { implicit request =>
    val action = FeedbackController.feedbackForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.feedbackForm()),
      feedback => {
        FeedbackService.save(feedback)
        emailService.onFeedbackSubmitted(feedback.id, feedback.email, feedback.content, feedback.created)

        if (ajax) {
          Ok(JsObject(Seq("status" -> JsString("OK"))))
        } else {
          Redirect(controllers.routes.SiteController.index()).flashing("success" -> "Thanks for your feedback!")
        }
      }
    )

    Future.successful(action.withHeaders(SiteController.cors: _*))
  }
}
