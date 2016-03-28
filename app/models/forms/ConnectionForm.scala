package models.forms

import models.engine.{ ConnectionSettings, DatabaseEngine }
import play.api.data.Form
import play.api.data.Forms._

object ConnectionForm {
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "engine" -> nonEmptyText,
      "url" -> nonEmptyText,
      "username" -> text,
      "password" -> text
    )(ConnectionForm.apply)(ConnectionForm.unapply)
  )
}

case class ConnectionForm(
  name: String,
  engine: String,
  url: String,
  username: String,
  password: String
)
