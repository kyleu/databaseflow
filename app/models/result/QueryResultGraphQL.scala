package models.result

import models.QueryResultResponse
import models.graphql.CommonSchema._
import models.graphql.GraphQLContext
import models.query.QueryResult
import models.schema.SchemaGraphQL._
import sangria.macros.derive._

object QueryResultGraphQL {
  implicit val resultColType = deriveObjectType[GraphQLContext, QueryResult.Col](ObjectTypeDescription("A column for this query result."))
  implicit val resultSourceType = deriveObjectType[GraphQLContext, QueryResult.Source](ObjectTypeDescription("The source of this query's results."))
  implicit val resultType = deriveObjectType[GraphQLContext, QueryResult](ObjectTypeDescription("A query result with full metadata."))
  implicit val resultResponseType = deriveObjectType[GraphQLContext, QueryResultResponse](ObjectTypeDescription("A wrapper for metadata and query results."))
}
