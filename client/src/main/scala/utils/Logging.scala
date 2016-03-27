package utils

import scala.scalajs.js.Dynamic.global

object Logging {
  def log(o: scalajs.js.Any) = {
    global.window.debug = o
    global.console.log(o)
  }
  def info(msg: String) = global.console.log(msg)

}
