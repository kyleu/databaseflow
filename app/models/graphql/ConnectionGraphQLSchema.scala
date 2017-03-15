package models.graphql

import models.connection.{ConnectionGraphQL, ConnectionSettings}
import sangria.renderer.SchemaRenderer
import sangria.schema._

case class ConnectionGraphQLSchema(cs: ConnectionSettings) {
  val queryFields = ConnectionGraphQL.queryFieldsForConnection(cs)

  val queryType = ObjectType(
    name = "Query",
    description = s"The main query interface for [${cs.name}].",
    fields = queryFields
  )

  val schema = sangria.schema.Schema(query = queryType)

  lazy val renderedSchema = SchemaRenderer.renderSchema(schema)
}
