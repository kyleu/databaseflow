package models.graphql

import models.connection.ConnectionGraphQL
import models.sandbox.SandboxGraphQL
import models.user.UserGraphQL
import sangria.schema._

object GlobalGraphQLSchema {
  val queryFields = UserGraphQL.queryFields ++ ConnectionGraphQL.queryFields ++ SandboxGraphQL.queryFields

  val queryType = ObjectType(
    name = "Query",
    description = "The main query interface.",
    fields = queryFields
  )

  val mutationFields = SandboxGraphQL.mutationFields

  val mutationType = ObjectType(
    name = "Mutation",
    description = "The main mutation interface.",
    fields = mutationFields
  )

  val schema = sangria.schema.Schema(
    query = queryType,
    mutation = Some(mutationType)
  )
}
