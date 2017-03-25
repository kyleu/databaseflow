package models.forms

import models.connection.ConnectionSettings
import models.engine.DatabaseEngine
import models.user.Permission
import play.api.data.Form
import play.api.data.Forms._
import utils.SlugUtils

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
      "read" -> nonEmptyText.transform(s => Permission.withName(s), (p: Permission) => p.toString),
      "edit" -> nonEmptyText.transform(s => Permission.withName(s), (p: Permission) => p.toString)
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
    read: Permission,
    edit: Permission
) {
  def isUrl = toggle == "url"

  def copyWith(conn: ConnectionSettings) = conn.copy(
    name = name,
    slug = SlugUtils.slugFor(name),
    read = read,
    edit = edit,
    description = description,
    engine = DatabaseEngine.withName(engine),
    host = if (isUrl) { None } else { host },
    port = if (isUrl) { None } else { port },
    dbName = if (isUrl) { None } else { dbName },
    extra = if (isUrl) { None } else { extra },
    urlOverride = if (isUrl) { urlOverride } else { None },
    username = username
  )
}
