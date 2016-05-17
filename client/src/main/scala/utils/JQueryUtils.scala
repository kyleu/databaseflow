package utils

import org.scalajs.dom
import org.scalajs.jquery.{ JQuery, JQueryEventObject, jQuery => $ }

object JQueryUtils {
  def clickHandler(jq: JQuery, f: (JQuery) => Unit) = {
    jq.click { (e: JQueryEventObject) =>
      f($(e.currentTarget))
      false
    }
  }

  def keyUpHandler(jq: JQuery, f: (JQuery, Int) => Unit) = {
    jq.keyup { (e: JQueryEventObject) =>
      f($(e.currentTarget), e.which)
      false
    }
  }

  def relativeTime() = {
    $("time.timeago").each { (e: dom.Element) =>
      val el = $(e)
      val moment = scalajs.js.Dynamic.global.moment(el.attr("datetime"))
      el.text(moment.fromNow().toString)
    }
  }
}
