package services

import java.util.UUID

import play.api.libs.mailer.{Email, MailerClient}

@javax.inject.Singleton
class EmailService @javax.inject.Inject() (mailerClient: MailerClient) {
  def onLicenseCreate(id: UUID, name: String, email: String, edition: String, issued: Long, version: Int, content: String) = {
    sendNotification(s"$edition License issued to [$email]", views.html.email.personalLicenseNotification(id, name, email, edition, issued, version, content).toString)

    sendMessage(
      name = name,
      address = email,
      subject = s"Your Database Flow $edition License",
      htmlBody = views.html.email.personalLicenseMessage(id, name, email, edition, issued, version, content).toString
    )
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
