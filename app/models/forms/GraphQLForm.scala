package models.forms

import java.util.UUID

import models.user.Permission
import play.api.data.Form
import play.api.data.Forms._

object GraphQLForm {
  val form = Form(
    mapping(
      "id" -> optional(uuid),
      "connection" -> optional(uuid),
      "category" -> optional(text),
      "name" -> nonEmptyText,
      "query" -> nonEmptyText,
      "read" -> nonEmptyText.transform(s => Permission.withName(s), (p: Permission) => p.toString),
      "edit" -> nonEmptyText.transform(s => Permission.withName(s), (p: Permission) => p.toString)
    )(GraphQLForm.apply)(GraphQLForm.unapply)
  )
}

case class GraphQLForm(
    id: Option[UUID],
    connection: Option[UUID],
    category: Option[String],
    name: String,
    query: String,
    read: Permission,
    edit: Permission
)
