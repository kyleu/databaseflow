package models.charting

import scala.scalajs.js

@js.native
trait ChartColumn extends js.Object {
  //def name: String = js.native
  //def `type`: String = js.native
  def parse(text: String): js.Any = js.native
}

