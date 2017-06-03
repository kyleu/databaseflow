package services.notification

import java.util.UUID

import org.joda.time.LocalDateTime
import views.html.email._

@javax.inject.Singleton
class NotificationService @javax.inject.Inject() (emailService: EmailService, slackService: SlackService) {
  def onFeedbackSubmitted(id: UUID, email: String, content: String, created: LocalDateTime) = {
    emailService.sendAdminMessage(s"Feedback received from [$email]", feedbackNotification(id, email, content, created).toString)
    slackService.alert(s"Feedback received from [$email]: $content", "#feedback")
  }

  def onNewsletterSignup(email: String, created: LocalDateTime) = {
    emailService.sendAdminMessage(s"Newsletter signup from [$email]", newsletterSignup(email, created).toString)
    slackService.alert(s"Newsletter signup from [$email]", "#newsletter")
  }

  def onFeedback(id: UUID, from: String, content: String, occurred: LocalDateTime) = {
    emailService.sendAdminMessage(s"Feedback received from [$from]", feedbackNotification(id, from, content, occurred).toString)
  }
}
