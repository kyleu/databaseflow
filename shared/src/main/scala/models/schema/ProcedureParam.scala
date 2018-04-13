package models.schema

import util.JsonSerializers._

object ProcedureParam {
  implicit val jsonEncoder: Encoder[ProcedureParam] = deriveEncoder
  implicit val jsonDecoder: Decoder[ProcedureParam] = deriveDecoder
}

case class ProcedureParam(
    name: String,
    description: Option[String],
    paramType: String,
    columnType: ColumnType,
    sqlTypeCode: Int,
    sqlTypeName: String,
    nullable: Boolean
)
