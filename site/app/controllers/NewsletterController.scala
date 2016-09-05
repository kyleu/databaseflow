package controllers

import org.joda.time.LocalDateTime
import play.api.i18n.MessagesApi
import services.notification.NotificationService

import scala.concurrent.Future

@javax.inject.Singleton
class NewsletterController @javax.inject.Inject() (implicit val messagesApi: MessagesApi, notificationService: NotificationService) extends BaseSiteController {
  def signup() = act("newsletter.signup") { implicit request =>
    val body = request.body.asFormUrlEncoded.getOrElse(throw new IllegalStateException("Invalid request"))
    val email = body.get("email").flatMap(_.headOption).getOrElse(throw new IllegalStateException("Missing [email] parameter."))
    if (email.trim.isEmpty) {
      Future.successful(Redirect(controllers.routes.SiteController.index()).flashing("error" -> "Please enter your email address."))
    } else {
      notificationService.onNewsletterSignup(email, new LocalDateTime())
      Future.successful(Redirect(controllers.routes.SiteController.index()).flashing("success" -> "Thanks for signing up!"))
    }
  }
}
