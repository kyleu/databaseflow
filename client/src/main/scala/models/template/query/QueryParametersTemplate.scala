package models.template.query

import java.util.UUID

import scalatags.Text.all._

object QueryParametersTemplate {
  def forValues(queryId: UUID, values: Seq[(String, String, String)]) = {
    div(cls := "row")(values.map(renderInput(queryId, _)): _*)
  }

  private[this] def renderInput(queryId: UUID, x: (String, String, String)) = {
    div(cls := "col s12 m4 l3 input-field")(
      input(id := queryId + "-parameter-" + x._1, data("key") := x._1, data("t") := x._2, `type` := "text", `value` := x._3),
      label(`for` := queryId + "-parameter-" + x._1, cls := "active")(x._1)
    )
  }
}
