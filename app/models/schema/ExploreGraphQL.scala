package models.schema

import models.connection.{ConnectionGraphQL, ConnectionSettings}
import models.graphql.GraphQLContext
import models.schema.SchemaModelGraphQL._
import models.user.User
import sangria.macros.derive._
import sangria.schema._

object ExploreGraphQL {
  val exploreType = ListType(StringType)
}
