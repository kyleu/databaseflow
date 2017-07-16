package util

import scala.scalajs.js.Dynamic.global

object Logging {
  def logJs(o: scalajs.js.Any) = {
    global.window.debug = o
    global.console.log(o)
  }

  def info(msg: String): Unit = {
    global.console.info(msg)
  }

  def error(msg: String): Unit = {
    global.console.error(msg)
  }
}
