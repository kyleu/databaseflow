package models.query

import java.util.UUID

import models.user.Permission
import util.JsonSerializers._

object SavedQuery {
  object Param {
    implicit val jsonEncoder: Encoder[Param] = deriveEncoder
    implicit val jsonDecoder: Decoder[Param] = deriveDecoder
  }

  case class Param(k: String, v: String)

  implicit val jsonEncoder: Encoder[SavedQuery] = deriveEncoder
  implicit val jsonDecoder: Decoder[SavedQuery] = deriveDecoder
}

case class SavedQuery(
    id: UUID = UUID.randomUUID,
    name: String = "Untitled Query",
    description: Option[String] = None,
    sql: String = "",
    params: Seq[SavedQuery.Param] = Seq.empty,

    owner: UUID,
    connection: Option[UUID] = None,
    read: Permission = Permission.User,
    edit: Permission = Permission.Private,

    lastRan: Option[Long] = None,

    created: Long = System.currentTimeMillis,
    updated: Long = System.currentTimeMillis,
    loadedAt: Long = System.currentTimeMillis
)
