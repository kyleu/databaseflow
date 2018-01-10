package models.schema

import models.connection.ConnectionGraphQL
import models.graphql.CommonGraphQL._
import models.graphql.GraphQLContext
import sangria.macros.derive._
import sangria.schema._
import models.schema.SchemaModelGraphQL._

object SchemaGraphQL {
  implicit val enumType = deriveObjectType[GraphQLContext, EnumType]()

  implicit val schemaType = deriveObjectType[GraphQLContext, Schema](
    ObjectTypeDescription("The database schema describing this connection."),
    ReplaceField("enums", Field(
      name = "enums",
      description = Some("The database enumerations available to this connection."),
      fieldType = ListType(enumType),
      arguments = ConnectionGraphQL.nameArg :: Nil,
      resolve = c => c.arg(ConnectionGraphQL.nameArg) match {
        case Some(filter) => c.value.enums.filter(e => e.key.equalsIgnoreCase(filter))
        case None => c.value.enums
      }
    )),
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
