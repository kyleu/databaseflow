package models.graphql

import models.connection.{ConnectionGraphQL, ConnectionSettings}
import sangria.renderer.SchemaRenderer
import sangria.schema._

case class ConnectionGraphQLSchema(cs: ConnectionSettings) {
  val queryType = ObjectType(
    name = "Query",
    description = s"The main query interface for [${cs.name}].",
    fields = ConnectionGraphQL.queryFieldsForConnection(cs)
  )

  val mutationType = ObjectType(
    name = "Mutation",
    description = s"The main mutation interface for [${cs.name}].",
    fields = ConnectionGraphQL.mutationFieldsForConnection(cs)
  )

  val schema = sangria.schema.Schema(query = queryType, mutation = None, subscription = None, additionalTypes = Nil)

  lazy val renderedSchema = SchemaRenderer.renderSchema(schema)
}
