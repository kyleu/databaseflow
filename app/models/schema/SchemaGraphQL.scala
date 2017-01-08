package models.schema

import models.connection.ConnectionGraphQL
import models.graphql.{CommonGraphQL, GraphQLContext}
import sangria.macros.derive._
import models.graphql.CommonGraphQL._
import sangria.schema._

object SchemaGraphQL {
  implicit val columnTypeEnum = CommonGraphQL.deriveEnumeratumType(
    name = "ColumnType",
    description = "The datatype of the column.",
    values = ColumnType.values.map(t => t -> t.entryName).toList
  )

  implicit val filterOpEnum = CommonGraphQL.deriveEnumeratumType(
    name = "FilterOp",
    description = "A filtering operation, usually applied to two values.",
    values = FilterOp.values.map(t => t -> t.entryName).toList
  )

  implicit val referenceType = deriveObjectType[GraphQLContext, Reference](ObjectTypeDescription("A reference to a different table or view."))
  implicit val primaryKeyType = deriveObjectType[GraphQLContext, PrimaryKey](ObjectTypeDescription("A primary key for this table or view."))
  implicit val foreignKeyType = deriveObjectType[GraphQLContext, ForeignKey](ObjectTypeDescription("A foreign key for this table or view."))

  implicit val indexColumnType = deriveObjectType[GraphQLContext, IndexColumn](ObjectTypeDescription("A column for this database index."))
  implicit val indexType = deriveObjectType[GraphQLContext, Index](ObjectTypeDescription("A database index for this table."))

  implicit val columnType = deriveObjectType[GraphQLContext, Column](ObjectTypeDescription("A database column for this table or view."))

  implicit val tableType = deriveObjectType[GraphQLContext, Table](
    ObjectTypeDescription("A database table for this connection."),
    AddFields(Field(
      name = "data",
      description = Some("View this table's data, passing filters as arguments."),
      fieldType = ListType(StringType),
      arguments = CommonGraphQL.limitArg :: CommonGraphQL.offsetArg :: Nil,
      resolve = c => Nil
    ))
  )

  implicit val viewType = deriveObjectType[GraphQLContext, View](ObjectTypeDescription("A database view for this connection."))

  implicit val procedureParamType = deriveObjectType[GraphQLContext, ProcedureParam](ObjectTypeDescription("The parameter for this stored procedure."))
  implicit val procedureType = deriveObjectType[GraphQLContext, Procedure](ObjectTypeDescription("A stored procedure for this connection."))

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
