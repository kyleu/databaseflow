package com.databaseflow.models.scalaexport.db.config

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class ExportFlag(override val value: String, val description: String) extends StringEnumEntry

object ExportFlag extends StringEnum[ExportFlag] with StringCirceEnum[ExportFlag] {
  case object Admin extends ExportFlag(value = "admin", description = "Exposes models through an admin interface, rather than the public site")
  case object Doobie extends ExportFlag(value = "doobie", description = "Exports Doobie database access classes")
  case object GraphQL extends ExportFlag(value = "graphql", description = "Exports a GraphQL schema")
  case object OpenApi extends ExportFlag(value = "openapi", description = "Exports Swagger documentation")
  case object Slick extends ExportFlag(value = "slick", description = "Exports Slick database access classes")
  case object Tests extends ExportFlag(value = "test", description = "Generates unit tests for exported objects")
  case object Validate extends ExportFlag(value = "validate", description = "Run a detailed audit on the generated project")
  case object View extends ExportFlag(value = "view", description = "Exports database view classes")

  override val values = findValues
}
