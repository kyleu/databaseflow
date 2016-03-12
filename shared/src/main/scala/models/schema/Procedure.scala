package models.schema

case class Procedure(
  name: String,
  description: Option[String],
  params: Seq[ProcedureParam],
  returnsResult: Option[Boolean]
)
