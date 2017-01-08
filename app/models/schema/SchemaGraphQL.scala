package models.schema

import models.connection.ConnectionGraphQL
import models.graphql.CommonGraphQL._
import models.graphql.GraphQLContext
import sangria.macros.derive._
import sangria.schema._
import SchemaModelGraphQL._

object SchemaGraphQL {
  implicit val schemaType = deriveObjectType[GraphQLContext, Schema](
    ObjectTypeDescription("The database schema describing this connection."),
    ReplaceField("tables", Field(
      name = "tables",
      description = Some("The database tables that are part of this connection."),
      fieldType = ListType(tableType),
      arguments = ConnectionGraphQL.nameArg :: Nil,
      resolve = c => c.arg(ConnectionGraphQL.nameArg) match {
        case Some(filter) => c.value.tables.filter(_.name.equalsIgnoreCase(filter))
        case None => c.value.tables
      }
    )),
    ReplaceField("views", Field(
      name = "views",
      description = Some("The database views that are part of this connection."),
      fieldType = ListType(viewType),
      arguments = ConnectionGraphQL.nameArg :: Nil,
      resolve = c => c.arg(ConnectionGraphQL.nameArg) match {
        case Some(filter) => c.value.views.filter(_.name.equalsIgnoreCase(filter))
        case None => c.value.views
      }
    )),
    ReplaceField("procedures", Field(
      name = "procedures",
      description = Some("The database procedures that are part of this connection."),
      fieldType = ListType(procedureType),
      arguments = ConnectionGraphQL.nameArg :: Nil,
      resolve = c => c.arg(ConnectionGraphQL.nameArg) match {
        case Some(filter) => c.value.procedures.filter(_.name.equalsIgnoreCase(filter))
        case None => c.value.procedures
      }
    ))
  )
}
