package models.charting.options

import models.charting.ChartSettings

trait ChartOptions {
  def selects: Seq[(String, String)]
  def flags: Seq[(String, String, Boolean)] = Nil

  val initialSettings = ChartSettings()
}
