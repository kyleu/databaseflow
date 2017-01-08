package services.graphql

import java.util.UUID

import models.audit.AuditType
import models.graphql.GraphQLQuery
import models.queries.graphql.GraphQLQueries
import models.user.{Role, User}
import services.audit.AuditRecordService
import services.database.core.MasterDatabase
import utils.Logging

object GraphQLQueryService extends Logging {
  def getVisible(user: User, id: Option[UUID] = None, name: Option[String] = None) = {
    val c = MasterDatabase.query(GraphQLQueries.getVisible(user))
    id match {
      case Some(i) => c.filter(_.id == i)
      case None => name match {
        case Some(n) => c.filter(_.name == n)
        case None => c
      }
    }
  }

  def canRead(user: User, gql: GraphQLQuery) = Role.matchPermissions(Some(user), gql.owner, "GraphQL", "read", gql.read)
  def canEdit(user: User, gql: GraphQLQuery) = Role.matchPermissions(Some(user), gql.owner, "GraphQL", "edit", gql.edit)

  def getById(id: UUID) = MasterDatabase.query(GraphQLQueries.getById(id))

  def insert(gql: GraphQLQuery) = {
    MasterDatabase.executeUpdate(GraphQLQueries.insert(gql))
    AuditRecordService.create(AuditType.SaveGraphQL, gql.owner, None, Some(gql.id.toString))
  }

  def update(gql: GraphQLQuery, userId: UUID) = {
    MasterDatabase.executeUpdate(GraphQLQueries.Update(gql))
    AuditRecordService.create(AuditType.SaveGraphQL, userId, None, Some(gql.id.toString))
  }

  def delete(id: UUID, userId: UUID) = {
    MasterDatabase.executeUpdate(GraphQLQueries.removeById(id))
    AuditRecordService.create(AuditType.DeleteGraphQL, userId, None, Some(id.toString))
  }
}
