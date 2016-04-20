package models.user

import play.api.data.Forms._
import play.api.data._

object UserForms {
  case class Credentials(identifier: String, password: String)

  val signInForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(Credentials.apply)(Credentials.unapply)
  )

  val registrationForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "passwordConfirm" -> nonEmptyText
    )(RegistrationData.apply)(RegistrationData.unapply)
  )

  val profileForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "theme" -> nonEmptyText
    )(ProfileData.apply)(ProfileData.unapply)
  )
}
