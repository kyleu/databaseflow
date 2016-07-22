package models.forms

import play.api.data.Form
import play.api.data.Forms._

object ConnectionForm {
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "engine" -> nonEmptyText,
      "host" -> text,
      "port" -> optional(number),
      "dbName" -> text,
      "extra" -> text,
      "urlOverride" -> text,
      "databaseUsername" -> text,
      "databasePassword" -> text,
      "description" -> text,
      "read" -> nonEmptyText,
      "edit" -> nonEmptyText
    )(ConnectionForm.apply)(ConnectionForm.unapply)
  )
}

case class ConnectionForm(
  name: String,
  engine: String,
  host: String,
  port: Option[Int],
  dbName: String,
  extra: String,
  urlOverride: String,
  username: String,
  password: String,
  description: String,
  read: String,
  edit: String
)
