package models.schema

case class ProcedureParam(
  name: String,
  description: Option[String],
  paramType: String,
  typeName: String,
  typeCode: Int,
  nullable: Boolean
)
