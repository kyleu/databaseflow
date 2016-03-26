package models.schema

case class ProcedureParam(
  name: String,
  description: Option[String],
  paramType: String,
  columnType: ColumnType,
  sqlTypeCode: Int,
  sqlTypeName: String,
  nullable: Boolean
)
