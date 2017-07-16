package util

import org.scalajs.jquery.{JQuery, JQueryEventObject, jQuery => $}

object TemplateUtils {
  def clickHandler(jq: JQuery, f: (JQuery) => Unit) = jq.click { e: JQueryEventObject =>
    f($(e.currentTarget))
    false
  }

  def changeHandler(jq: JQuery, f: (JQuery) => Unit) = jq.change { e: JQueryEventObject =>
    f($(e.currentTarget))
    false
  }
}
