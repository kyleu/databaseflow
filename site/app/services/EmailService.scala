package services

import java.util.UUID

import org.joda.time.LocalDateTime
import play.api.Logger
import play.api.libs.mailer.{Email, MailerClient}
import views.html.email.{feedbackNotification, personalLicenseMessage, personalLicenseNotification}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

@javax.inject.Singleton
class EmailService @javax.inject.Inject() (mailerClient: MailerClient) {
  def onFeedbackSubmitted(id: UUID, email: String, content: String, created: LocalDateTime) = {
    sendNotification(s"Feedback received from [$email]", feedbackNotification(id, email, content, created).toString)
  }

  def onLicenseCreate(id: UUID, name: String, email: String, edition: String, issued: Long, version: Int, content: String) = {
    sendNotification(s"$edition License issued to [$email]", personalLicenseNotification(id, name, email, edition, issued, version, content).toString)
    val messageBody = personalLicenseMessage(id, name, email, edition, issued, version, content).toString
    sendMessage(name = name, address = email, subject = s"Your Database Flow $edition License", htmlBody = messageBody)
  }

  def onFeedback() = {

  }

  def sendNotification(subject: String, htmlBody: String) = {
    sendMessage("Database Flow Admin", "kyle@databaseflow.com", subject, htmlBody)
  }

  def sendMessage(name: String, address: String, subject: String, htmlBody: String) = {
    val f = Future {
      val email = Email(
        subject,
        "The Database Flow Team <databaseflow@databaseflow.com>",
        Seq(s"$name <$address>"),
        bodyHtml = Some(htmlBody)
      )
      mailerClient.send(email)
    }
    f.onSuccess {
      case x => Logger.warn(s"Successfully sent email to [$address].")
    }
    f.onFailure {
      case x => Logger.warn(s"Unable to send email to [$address].", x)
    }
  }
}
