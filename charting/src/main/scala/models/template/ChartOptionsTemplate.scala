package models.template

import scala.scalajs.js

import scalatags.Text.all._

object ChartOptionsTemplate {
  def forChart(columns: js.Array[js.Object], chart: js.Object) = div(cls := "row")(
    div(cls := "col s12")(
      select(cls := "chart-type-select")(
        models.charting.ChartType.values.map { ct =>
          option(ct.toString)
        }
      )
    )
  )
}
