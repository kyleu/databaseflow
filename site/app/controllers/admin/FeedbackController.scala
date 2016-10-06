package controllers.admin

import java.util.UUID

import controllers.BaseSiteController
import play.api.i18n.MessagesApi
import services.audit.FeedbackService

import scala.concurrent.Future

@javax.inject.Singleton
class FeedbackController @javax.inject.Inject() (implicit override val messagesApi: MessagesApi) extends BaseSiteController {
  def list() = withAdminSession("feedback-list") { (username, request) =>
    implicit val req = request
    val feedbacks = FeedbackService.list().map(FeedbackService.load).toSeq
    Future.successful(Ok(views.html.admin.audit.feedbacks(feedbacks)))
  }

  def remove(id: UUID) = withAdminSession("feedback-remove") { (username, request) =>
    implicit val req = request
    FeedbackService.remove(id)
    Future.successful(Redirect(controllers.admin.routes.FeedbackController.list()).flashing("success" -> "Feedback removed."))
  }
}
