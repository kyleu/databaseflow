package models.queries.graphql

import java.util.UUID

import models.database.{Row, Statement}
import models.graphql.GraphQLQuery
import models.queries.BaseQueries
import models.user.{Permission, Role, User}
import utils.JdbcUtils

object GraphQLQueries extends BaseQueries[GraphQLQuery] {
  override protected val tableName = "graphql"
  override protected val columns = Seq("id", "connection_id", "category", "name", "query", "owner", "read", "edit", "created")
  override protected val searchColumns = columns

  val insert = Insert
  def removeById(id: UUID) = RemoveById(Seq(id))
  def getAll(orderBy: String = "\"name\"") = GetAll(orderBy = orderBy)
  def getVisible(owner: User, orderBy: String = "\"name\"") = {
    val readPerms = if (owner.role == Role.Admin) {
      "\"read\" in ('user', 'admin')"
    } else {
      "\"read\" = 'user'"
    }
    val ownerPerms = " or \"owner\" = ?"
    val values = Seq(owner.id)
    GetAll(
      whereClause = Some(readPerms + ownerPerms),
      orderBy = orderBy,
      values = values
    )
  }
  def getById(id: UUID) = GetById(Seq(id))
  val search = Search
  val removeById = RemoveById

  case class Update(gql: GraphQLQuery) extends Statement {
    override val sql = updateSql(Seq("category", "name", "query", "owner", "read", "edit"))
    override val values = Seq(gql.category, gql.name, gql.query, gql.owner, gql.read.toString, gql.edit.toString, gql.id)
  }

  override protected def fromRow(row: Row) = GraphQLQuery(
    id = row.as[UUID]("id"),
    connection = row.asOpt[UUID]("connection_id"),
    category = row.asOpt[String]("category"),
    name = row.as[String]("name"),
    query = row.as[String]("query"),
    owner = row.as[UUID]("owner"),
    read = Permission.withName(row.as[String]("read")),
    edit = Permission.withName(row.as[String]("edit")),
    created = JdbcUtils.toLocalDateTime(row, "created")
  )

  override protected def toDataSeq(gql: GraphQLQuery) = Seq[Any](
    gql.id, gql.connection, gql.category, gql.name, gql.query, gql.owner, gql.read.toString, gql.edit.toString, gql.created
  )
}
