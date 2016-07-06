package models.forms

import play.api.data.Form
import play.api.data.Forms._

object ConnectionForm {
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "engine" -> nonEmptyText,
      "url" -> nonEmptyText,
      "databaseUsername" -> text,
      "databasePassword" -> text,
      "read" -> nonEmptyText,
      "edit" -> nonEmptyText
    )(ConnectionForm.apply)(ConnectionForm.unapply)
  )
}

case class ConnectionForm(
  name: String,
  engine: String,
  url: String,
  username: String,
  password: String,
  read: String,
  edit: String
)
