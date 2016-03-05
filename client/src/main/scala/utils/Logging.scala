package utils

import scala.scalajs.js.Dynamic.global

object Logging {
  def info(msg: String) = global.console.log(msg)
}
