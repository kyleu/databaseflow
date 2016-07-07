package services

import java.util.UUID

import org.springframework.context.ApplicationContext
import play.api.libs.mailer.{Email, MailerClient}

@javax.inject.Singleton
class EmailService @javax.inject.Inject() (mailerClient: MailerClient) {
  def onLicenseCreate(id: UUID, name: String, email: String, edition: String, issued: Long, version: Int, content: String) = {
    sendNotification("Personal License Issued", s"""<html><body>
      <div>$id</div>
    </body></html>""")

    sendMessage(name, email, "Your Database Flow Personal Edition License", s"""<html><body>
      <div>Good work!</div>
    </body></html>""")
  }

  def sendNotification(subject: String, htmlBody: String) = {
    val email = Email(
      subject,
      "The Database Flow Team <databaseflow@databaseflow.com>",
      Seq("Database Flow Admin <kyle@databaseflow.com>"),
      bodyHtml = Some(htmlBody)
    )
    mailerClient.send(email)
  }

  def sendMessage(name: String, address: String, subject: String, htmlBody: String) = {
    val email = Email(
      subject,
      "The Database Flow Team <databaseflow@databaseflow.com>",
      Seq(s"$name FROM <$address>"),
      bodyHtml = Some(htmlBody)
    )
    mailerClient.send(email)
  }
}
