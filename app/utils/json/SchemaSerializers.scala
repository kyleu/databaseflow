package utils.json

import models.schema._
import play.api.libs.json.Json

object SchemaSerializers {
  implicit val columnFormat = Json.format[Column]

  implicit val indexColumnFormat = Json.format[IndexColumn]
  implicit val indexFormat = Json.format[Index]

  implicit val referenceFormat = Json.format[Reference]
  implicit val foreignKeyFormat = Json.format[ForeignKey]

  implicit val tableFormat = Json.format[Table]
  implicit val databaseFormat = Json.format[Database]
}
