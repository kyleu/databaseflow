package models.charting.options

trait ChartOptions {
  def selects: Seq[(String, String)]
  def flags: Seq[(String, String, Boolean)] = Nil
}
