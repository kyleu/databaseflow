package models.graphql

import models.connection.{ConnectionGraphQL, ConnectionSettings}
import sangria.schema._

case class ConnectionGraphQLSchema(cs: ConnectionSettings) {
  val queryFields = ConnectionGraphQL.queryFieldsForConnection(cs)

  val queryType = ObjectType(
    name = "Query",
    description = "The main query interface.",
    fields = queryFields
  )

  val schema = sangria.schema.Schema(query = queryType)
}
