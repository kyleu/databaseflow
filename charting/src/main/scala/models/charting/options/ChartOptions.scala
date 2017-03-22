package models.charting.options

import models.charting.ChartSettings

import scala.scalajs.js

trait ChartOptions {
  def selects: Seq[(String, String)]
  def flags: Seq[(String, String, Boolean)] = Nil

  def getJsData(settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]): Seq[js.Dynamic]
  def getJsOptions(settings: ChartSettings): js.Dynamic

  protected[this] def getDataColumn(key: String, settings: ChartSettings, columns: Seq[(String, String)], data: js.Array[js.Array[String]]) = {
    settings.selects.get(key) match {
      case Some(x) =>
        val col = settings.selects.getOrElse(key, "")
        val idx = columns.indexWhere(_._1 == col)
        val ret = if (idx == -1) { js.Array() } else { data.map(_(idx)) }
        ret
      case None => js.Array()
    }
  }

  protected[this] def selectValue(settings: ChartSettings, key: String) = settings.selects.getOrElse(key, "")
}
