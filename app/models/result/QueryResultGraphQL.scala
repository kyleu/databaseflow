package models.result

import models.QueryResultResponse
import models.graphql.CommonGraphQL._
import models.graphql.{CommonGraphQL, GraphQLContext}
import models.query.{QueryFilter, QueryResult, SavedQuery, SharedResult}
import models.connection.ConnectionGraphQL.permissionEnum
import models.queries.dynamic.DynamicQuery
import models.schema.{ColumnType, FilterOp, SchemaModelGraphQL}
import sangria.macros.derive._
import sangria.schema.{Argument, BooleanType, Field, IntType, ObjectType, OptionInputType, StringType}
import services.query.SharedResultService

object QueryResultGraphQL {
  implicit val columnTypeEnum = CommonGraphQL.deriveStringEnumeratumType(
    name = "ColumnType",
    description = "The datatype of the column.",
    values = ColumnType.values.map(t => t -> t.value).toList
  )

  implicit val filterOpEnum = CommonGraphQL.deriveEnumeratumType(
    name = "FilterOp",
    description = "A filtering operation, usually applied to two values.",
    values = FilterOp.values.map(t => t -> t.entryName).toList
  )

  implicit val sourceTypeEnum = CommonGraphQL.deriveEnumeratumType(
    name = "SourceType",
    description = "A source for cached results and saved queries.",
    values = QueryResult.SourceType.values.map(t => t -> t.entryName).toList
  )

  val whereClauseArg = Argument("whereClause", OptionInputType(StringType), description = "Adds the provided string to the where clause (i.e. \"id = 1\").")

  val sortColArg = Argument("sortCol", OptionInputType(StringType), description = "Sorts the returned results by the provided column.")
  val sortAscArg = Argument("sortAsc", OptionInputType(BooleanType), description = "Sorts the returned results ascending or descending.")

  val filterColArg = Argument("filterCol", OptionInputType(StringType), description = "Filters the returned results by the provided column.")
  val filterOpArg = Argument("filterOp", OptionInputType(filterOpEnum), description = "Filters the returned results using the provided operation.")
  val filterTypeArg = Argument("filterType", OptionInputType(columnTypeEnum), description = "Filters the returned results with the provided type.")
  val filterValueArg = Argument("filterValue", OptionInputType(StringType), description = "Filters the returned results using the provided value.")

  val limitArg = Argument("limit", OptionInputType(IntType), description = "Caps the number of returned results.")
  val offsetArg = Argument("offset", OptionInputType(IntType), description = "Starts the returned results after this number of rows.")

  val resultArgs = sortColArg :: sortAscArg :: whereClauseArg :: filterColArg :: filterOpArg :: filterTypeArg :: filterValueArg :: limitArg :: offsetArg :: Nil

  implicit val queryFilterType = deriveObjectType[GraphQLContext, QueryFilter](ObjectTypeDescription("A filter applied to this query."))

  implicit val resultColType = deriveObjectType[GraphQLContext, QueryResult.Col](ObjectTypeDescription("A column for this query result."))
  implicit val resultSourceType = deriveObjectType[GraphQLContext, QueryResult.Source](ObjectTypeDescription("The source of this query's results."))
  implicit val resultType: ObjectType[GraphQLContext, QueryResult] = deriveObjectType[GraphQLContext, QueryResult](
    ObjectTypeDescription("A query result with full metadata.")
  )
  implicit val resultResponseType = deriveObjectType[GraphQLContext, QueryResultResponse](ObjectTypeDescription("A wrapper for metadata and query results."))

  implicit val queryParamType = deriveObjectType[GraphQLContext, SavedQuery.Param](ObjectTypeDescription("A name and value for a query parameter."))
  implicit val savedQueryType = deriveObjectType[GraphQLContext, SavedQuery](ObjectTypeDescription("A query that has been saved for later use."))
  implicit val dynamicQueryResultType = deriveObjectType[GraphQLContext, DynamicQuery.Results](
    ObjectTypeDescription("The cached data for this shared result.")
  )
  implicit val sharedResultType = deriveObjectType[GraphQLContext, SharedResult](
    ObjectTypeDescription("A query result that has been shared with others."),
    AddFields(Field(
      name = "results",
      description = Some("Returns this shared result's data."),
      fieldType = dynamicQueryResultType,
      arguments = resultArgs,
      resolve = c => SharedResultService.getData(Some(c.ctx.user), c.value, SchemaModelGraphQL.rowDataOptionsFor(c))
    ))
  )
}
