package com.databaseflow.models.scalaexport.db.config

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class ExportFlag(override val value: String, val description: String) extends StringEnumEntry

object ExportFlag extends StringEnum[ExportFlag] with StringCirceEnum[ExportFlag] {
  case object Tests extends ExportFlag(value = "test", description = "Generates unit tests for exported objects")
  case object GraphQL extends ExportFlag(value = "graphql", description = "Exports a GraphQL schema")
  case object OpenApi extends ExportFlag(value = "openapi", description = "Exports Swagger documentation")
  case object Slick extends ExportFlag(value = "slick", description = "Exports Slick database access classes")
  case object Doobie extends ExportFlag(value = "doobie", description = "Exports Doobie database access classes")

  override val values = findValues
}
