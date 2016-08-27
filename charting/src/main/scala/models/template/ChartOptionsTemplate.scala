package models.template

import models.charting.ChartType

import scala.scalajs.js
import scalatags.Text.all._

object ChartOptionsTemplate {
  def columnSelect(cssClass: String, columns: Seq[(String, String)], activeOption: Option[String]) = select(cls := cssClass) {
    val initialCol = activeOption match {
      case Some(_) => option(disabled)
      case None => option(disabled, selected)
    }
    initialCol +: columns.map { col =>
      if (activeOption.contains(col._1)) {
        option(value := col._1, selected)(col._2)
      } else {
        option(value := col._1)(col._2)
      }
    }
  }

  def selects(t: ChartType, columns: Seq[(String, String)]) = div(t.options.selects.map { select =>
    div(cls := "input-field col s12 m4 l3")(
      columnSelect(s"chart-select chart-select-${select._1}", columns, None),
      label(select._2)
    )
  })

  def flags(t: ChartType) = {
    val prefix = scala.util.Random.alphanumeric.take(8).mkString
    div(cls := "row")(t.options.flags.map { flag =>
      val flagInput = input(`type` := "checkbox", data("key") := flag._1, id := s"$prefix-${flag._1}", cls := s"chart-flag chart-flag-${flag._1}")
      val checkedInput = if (flag._3) {
        flagInput(checked)
      } else {
        flagInput
      }
      div(cls := "input-field col s12 m4 l3")(
        checkedInput,
        label(`for` := s"$prefix-${flag._1}")(flag._2)
      )
    })
  }

  def options(columns: Seq[(String, String)], chart: js.Dynamic) = div(cls := "row")(
    div(cls := "input-field col s12 m4 l3")(
      select(cls := "chart-type-select")(
        ChartType.values.map(t => option(value := t.id)(t.title))
      ),
      label("Chart Type")
    ),
    div(cls := "chart-columns-container"),
    div(cls := "chart-flags-container")
  )
}
