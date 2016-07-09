package services.notification

import java.util.UUID

import org.joda.time.LocalDateTime
import views.html.email.{feedbackNotification, personalLicenseMessage, personalLicenseNotification}

@javax.inject.Singleton
class NotificationService @javax.inject.Inject() (emailService: EmailService, slackService: SlackService) {
  def onFeedbackSubmitted(id: UUID, email: String, content: String, created: LocalDateTime) = {
    emailService.sendAdminMessage(s"Feedback received from [$email]", feedbackNotification(id, email, content, created).toString)
    slackService.alert(s"Feedback received from [$email]: $content", "#feedback")
  }

  def onLicenseCreate(id: UUID, name: String, email: String, edition: String, issued: Long, version: Int, content: String) = {
    val adminMessageBody = personalLicenseNotification(id, name, email, edition, issued, version, content).toString
    emailService.sendAdminMessage(s"$edition License issued to [$email]", adminMessageBody)
    slackService.alert(s"$edition License created for [$email].", "#licenses")
    val messageBody = personalLicenseMessage(id, name, email, edition, issued, version, content).toString
    emailService.sendMessage(name = name, address = email, subject = s"Your Database Flow $edition License", htmlBody = messageBody)
  }

  def onFeedback(id: UUID, from: String, content: String, occurred: LocalDateTime) = {
    emailService.sendAdminMessage(s"Feedback received from [$from]", feedbackNotification(id, from, content, occurred).toString)
  }
}
