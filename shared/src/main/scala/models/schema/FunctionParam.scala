package models.schema

case class FunctionParam(
  name: String,
  description: Option[String],
  paramType: String,
  typeName: String,
  typeCode: Int,
  nullable: Boolean
)
