package models.graphql

import java.util.UUID

import models.user.Permission
import org.joda.time.LocalDateTime
import utils.DateUtils

object GraphQLQuery {
  def empty(owner: UUID) = GraphQLQuery(UUID.randomUUID, None, None, "NEW", "", owner, Permission.Private, Permission.Private, DateUtils.now)
}

case class GraphQLQuery(
  id: UUID,
  connection: Option[UUID],
  category: Option[String],
  name: String,
  query: String,
  owner: UUID,
  read: Permission,
  edit: Permission,
  created: LocalDateTime
)
