package models.forms

import play.api.data.Form
import play.api.data.Forms._

object ConnectionForm {
  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "engine" -> nonEmptyText,
      "toggle" -> nonEmptyText,
      "host" -> optional(text),
      "port" -> optional(number),
      "dbName" -> optional(text),
      "extra" -> optional(text),
      "urlOverride" -> optional(text),
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
    toggle: String,
    host: Option[String],
    port: Option[Int],
    dbName: Option[String],
    extra: Option[String],
    urlOverride: Option[String],
    username: String,
    password: String,
    description: String,
    read: String,
    edit: String
) {
  def isUrl = toggle == "url"
}
