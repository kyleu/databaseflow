package utils

import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }

object JQueryUtils {
  def clickHandler(jq: JQuery, f: (JQuery) => Unit) = {
    jq.click({ (e: JQueryEventObject) =>
      f($(e.currentTarget))
      false
    })
  }
}

