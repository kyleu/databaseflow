package models.schema

import models.graphql.{CommonGraphQL, GraphQLContext}
import models.graphql.CommonGraphQL._
import models.query.RowDataOptions
import models.result.QueryResultGraphQL
import sangria.macros.derive._
import sangria.schema._
import services.query.RowDataService

object SchemaModelGraphQL {
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

  val sortColArg = Argument("sortCol", OptionInputType(StringType), description = "Sorts the returned results by the provided column.")
  val sortAscArg = Argument("sortAsc", OptionInputType(BooleanType), description = "Sorts the returned results ascending or descending.")

  val filterColArg = Argument("filterCol", OptionInputType(StringType), description = "Filters the returned results by the provided column.")
  val filterOpArg = Argument("filterOp", OptionInputType(filterOpEnum), description = "Filters the returned results using the provided operation.")
  val filterTypeArg = Argument("filterType", OptionInputType(columnTypeEnum), description = "Filters the returned results with the provided type.")
  val filterValueArg = Argument("filterValue", OptionInputType(StringType), description = "Filters the returned results using the provided value.")

  val limitArg = Argument("limit", OptionInputType(IntType), description = "Caps the number of returned results.")
  val offsetArg = Argument("offset", OptionInputType(IntType), description = "Starts the returned results after this number of rows.")

  private[this] def rowDataOptionsFor(c: Context[GraphQLContext, _]) = {
    val sc = c.arg(sortColArg)
    val sa = c.arg(sortAscArg)
    val fc = c.arg(filterColArg)
    val fo = c.arg(filterOpArg)
    val ft = c.arg(filterTypeArg)
    val fv = c.arg(filterValueArg)
    val l = c.arg(limitArg)
    val o = c.arg(offsetArg)
    def strip(s: String) = if (s.isEmpty) { None } else { Some(s) }
    RowDataOptions(sc, sa, fc, fo, ft, fv, l, o)
  }

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
      description = Some("Return this table's data, passing filters as arguments."),
      fieldType = QueryResultGraphQL.resultType,
      arguments = sortColArg :: sortAscArg :: filterColArg :: filterOpArg :: filterTypeArg :: filterValueArg :: limitArg :: offsetArg :: Nil,
      resolve = c => RowDataService.getRowData(c.ctx.user, c.value.connection, "table", c.value.name, rowDataOptionsFor(c))
    ))
  )

  implicit val viewType = deriveObjectType[GraphQLContext, View](
    ObjectTypeDescription("A database view for this connection."),
    AddFields(Field(
      name = "data",
      description = Some("Return this view's data, passing filters as arguments."),
      fieldType = QueryResultGraphQL.resultType,
      arguments = sortColArg :: sortAscArg :: filterColArg :: filterOpArg :: filterTypeArg :: filterValueArg :: limitArg :: offsetArg :: Nil,
      resolve = c => RowDataService.getRowData(c.ctx.user, c.value.connection, "view", c.value.name, rowDataOptionsFor(c))
    ))
  )

  implicit val procedureParamType = deriveObjectType[GraphQLContext, ProcedureParam](ObjectTypeDescription("The parameter for this stored procedure."))
  implicit val procedureType = deriveObjectType[GraphQLContext, Procedure](ObjectTypeDescription("A stored procedure for this connection."))
}
