package models.schema

case class Procedure(
    name: String,
    description: Option[String],
    params: Seq[ProcedureParam],
    returnsResult: Option[Boolean],
    loadedAt: Long = System.currentTimeMillis
) {
  def getValues(paramMap: Map[String, String]) = params.flatMap { p =>
    p.paramType match {
      case "in" => Some(p.name -> paramMap.get(p.name).orNull)
      case _ => throw new IllegalStateException(p.paramType)
    }
  }
}
