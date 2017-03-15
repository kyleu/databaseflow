package models.result

import models.QueryResultResponse
import models.graphql.CommonGraphQL._
import models.graphql.GraphQLContext
import models.query.{QueryResult, RowDataOptions, SavedQuery, SharedResult}
import models.schema.SchemaModelGraphQL._
import models.connection.ConnectionGraphQL.permissionEnum
import models.queries.dynamic.DynamicQuery
import sangria.macros.derive._
import sangria.schema.{Field, ObjectType}
import services.query.SharedResultService

object QueryResultGraphQL {
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
      arguments = sortColArg :: sortAscArg :: filterColArg :: filterOpArg :: filterTypeArg :: filterValueArg :: limitArg :: offsetArg :: Nil,
      resolve = c => {
        val rdo = RowDataOptions(
          orderByCol = c.arg(sortColArg), orderByAsc = c.arg(sortAscArg),
          filterCol = c.arg(filterColArg), filterOp = c.arg(filterOpArg), filterType = c.arg(filterTypeArg), filterVal = c.arg(filterValueArg),
          limit = c.arg(limitArg), offset = c.arg(offsetArg)
        )
        SharedResultService.getData(Some(c.ctx.user), c.value, rdo)
      }
    ))
  )
}
