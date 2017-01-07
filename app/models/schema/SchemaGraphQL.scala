package models.schema

import models.graphql.{CommonSchema, GraphQLContext}
import sangria.macros.derive._
import models.graphql.CommonSchema._

object SchemaGraphQL {
  implicit val columnTypeEnum = CommonSchema.deriveEnumeratumType(
    name = "ColumnType",
    description = "The datatype of the column.",
    values = ColumnType.values.map(t => t -> t.entryName).toList
  )

  implicit val referenceType = deriveObjectType[GraphQLContext, Reference](ObjectTypeDescription("A reference to a different table or view."))
  implicit val primaryKeyType = deriveObjectType[GraphQLContext, PrimaryKey](ObjectTypeDescription("A primary key for this table or view."))
  implicit val foreignKeyType = deriveObjectType[GraphQLContext, ForeignKey](ObjectTypeDescription("A foreign key for this table or view."))

  implicit val indexColumnType = deriveObjectType[GraphQLContext, IndexColumn](ObjectTypeDescription("A column for this database index."))
  implicit val indexType = deriveObjectType[GraphQLContext, Index](ObjectTypeDescription("A database index for this table."))

  implicit val columnType = deriveObjectType[GraphQLContext, Column](ObjectTypeDescription("A database column for this table or view."))

  implicit val tableType = deriveObjectType[GraphQLContext, Table](ObjectTypeDescription("A database table for this connection."))

  implicit val viewType = deriveObjectType[GraphQLContext, View](ObjectTypeDescription("A database view for this connection."))

  implicit val procedureParamType = deriveObjectType[GraphQLContext, ProcedureParam](ObjectTypeDescription("The parameter for this stored procedure."))
  implicit val procedureType = deriveObjectType[GraphQLContext, Procedure](ObjectTypeDescription("A stored procedure for this connection."))

  implicit val schemaType = deriveObjectType[GraphQLContext, Schema](ObjectTypeDescription("The database schema describing this connection."))
}
