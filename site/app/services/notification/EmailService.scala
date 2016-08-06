package services.notification

import java.util.UUID

import org.joda.time.LocalDateTime
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.mailer.{Email, MailerClient}
import utils.Logging
import views.html.email.{feedbackNotification, personalLicenseMessage, personalLicenseNotification}

import scala.concurrent.Future

@javax.inject.Singleton
class EmailService @javax.inject.Inject() (mailerClient: MailerClient) extends Logging {
  def sendAdminMessage(subject: String, htmlBody: String) = {
    sendMessage(utils.Config.projectName + " Admin", "kyle@databaseflow.com", subject, htmlBody)
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
      case x => log.info(s"Successfully sent email to [$address].")
    }
    f.onFailure {
      case x => log.warn(s"Unable to send email to [$address].", x)
    }
  }
}
