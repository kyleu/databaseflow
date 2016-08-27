package models.template

import models.charting.ChartType

import scala.scalajs.js
import scalatags.Text.all._

object ChartOptionsTemplate {
  def selects(t: ChartType, columns: Seq[(String, String)]) = div(t.options.selects.map { sel =>
    div(cls := "input-field col s12 m4")(
      select(cls := s"chart-select chart-select-${sel._1}", data("key") := sel._1)(
        option(selected) +: columns.map { col =>
          option(value := col._1)(col._2)
        }
      ),
      label(sel._2)
    )
  })

  def flags(elId: String, t: ChartType) = {
    div(cls := "row")(t.options.flags.map { flag =>
      val flagInput = input(`type` := "checkbox", data("key") := flag._1, id := s"$elId-${flag._1}", cls := s"chart-flag chart-flag-${flag._1}")
      val checkedInput = if (flag._3) {
        flagInput(checked)
      } else {
        flagInput
      }
      div(cls := "input-field col s12 m4")(
        checkedInput,
        label(`for` := s"$elId-${flag._1}")(flag._2)
      )
    })
  }

  def options(elId: String, columns: Seq[(String, String)], chart: js.Dynamic) = div(
    div(cls := "chart-type-select-container")(
      select(cls := "chart-type-select")(
        ChartType.values.map(t => option(value := t.id)(t.title))
      )
    ),
    div(
      div(cls := "chart-columns-container"),
      div(cls := "chart-flags-container clear")
    )
  )
}
