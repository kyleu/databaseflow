package ui

import org.scalajs.dom
import org.scalajs.jquery.{ JQuery, jQuery => $ }

case class ModelFilterManager(queryPanel: JQuery) {
  var activeFilter: Option[String] = None
  val trs = $("tbody tr", queryPanel)

  def filter(s: Option[String]) = {
    if (activeFilter == s) {
      // no op
    } else {
      s match {
        case Some(v) => trs.each { (e: dom.Element) =>
          val el = $(e)
          val source = $("td:first-child", el).text().toLowerCase
          if (source.contains(v)) {
            el.show()
          } else {
            el.hide()
          }
        }
        case None => trs.each((e: dom.Element) => $(e).show())
      }
      activeFilter = s
    }
  }
}
