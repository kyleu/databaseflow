package services

import scala.scalajs.js

object NotificationService {
  private[this] val materialize = js.Dynamic.global.Materialize

  def info(message: String, duration: Int = 2500) = {
    materialize.toast(message, duration)
  }
}
