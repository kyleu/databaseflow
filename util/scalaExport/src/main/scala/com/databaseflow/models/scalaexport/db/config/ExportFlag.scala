package com.databaseflow.models.scalaexport.db.config

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class ExportFlag(override val value: String, val description: String) extends StringEnumEntry

object ExportFlag extends StringEnum[ExportFlag] with StringCirceEnum[ExportFlag] {
  case object GraphQL extends ExportFlag(value = "graphql", description = "")
  case object OpenApi extends ExportFlag(value = "openapi", description = "")
  case object Slick extends ExportFlag(value = "slick", description = "")
  case object Doobie extends ExportFlag(value = "doobie", description = "")

  override val values = findValues
}
