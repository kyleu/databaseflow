package models.template.proc

import models.schema.ProcedureParam

import scalatags.Text.all._

object ProcedureCallTemplate {
  private[this] def forParam(param: ProcedureParam) = div(cls := "col s12 m4")(param.name + ": " + param.columnType)
  def forParams(params: Seq[ProcedureParam]) = div(cls := "param-form-container")(params.map(p => forParam(p)): _*)
}
