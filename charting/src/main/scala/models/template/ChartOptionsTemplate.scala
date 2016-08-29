package models.template

import java.util.UUID

import models.charting.{ChartSettings, ChartType}

import scalatags.Text.all._

object ChartOptionsTemplate {
  def selects(chart: ChartSettings, columns: Seq[(String, String)]) = div(chart.t.options.selects.map { sel =>
    val options = chart.selects.get(sel._1) match {
      case Some(v) => option() +: columns.map { col =>
        if (v == col._1) {
          option(value := col._1, selected)(col._2)
        } else {
          option(value := col._1)(col._2)
        }
      }
      case None => option(selected) +: columns.map { col =>
        option(value := col._1)(col._2)
      }
    }
    div(cls := "input-field col s12 m4")(
      select(cls := s"chart-select chart-select-${sel._1}", data("key") := sel._1)(options),
      label(sel._2)
    )
  })

  def flags(chartId: UUID, chart: ChartSettings) = {
    div(cls := "row")(chart.t.options.flags.map { flag =>
      val flagInput = input(`type` := "checkbox", data("key") := flag._1, id := s"chart-option-$chartId-${flag._1}", cls := s"chart-flag chart-flag-${flag._1}")
      val checkedInput = if (chart.flags.getOrElse(flag._1, flag._3)) {
        flagInput(checked)
      } else {
        flagInput
      }
      div(cls := "input-field col s12 m4")(
        checkedInput,
        label(`for` := s"chart-option-$chartId-${flag._1}")(flag._2)
      )
    })
  }

  def options(columns: Seq[(String, String)], chart: ChartSettings) = div(
    div(cls := "chart-type-select-container")(
      select(cls := "chart-type-select")(
        ChartType.values.map { t =>
          if (chart.t == t) {
            option(value := t.id, selected)(t.title)
          } else {
            option(value := t.id)(t.title)
          }
        }
      )
    ),
    div(
      div(cls := "chart-columns-container"),
      div(cls := "chart-flags-container clear")
    )
  )
}
