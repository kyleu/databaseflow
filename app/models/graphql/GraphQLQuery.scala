package models.graphql

import java.util.UUID

import models.user.Permission
import org.joda.time.LocalDateTime

case class GraphQLQuery(
  id: UUID,
  connection: Option[UUID],
  category: String,
  name: String,
  query: String,
  owner: UUID,
  read: Permission,
  edit: Permission,
  created: LocalDateTime
)
