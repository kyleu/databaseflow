package services.logging

import enumeratum._
import play.twirl.api.Html

sealed trait LogLevel extends EnumEntry {
  def startPhrase = "[" + toString.toUpperCase + "]"
  def toHtml: Html
}

object LogLevel extends Enum[LogLevel] {
  override val values = findValues

  case object Trace extends LogLevel {
    override def toHtml = Html("""<span class="label label-success">Trace</span>""")
  }
  case object Debug extends LogLevel {
    override def toHtml = Html("""<span class="label label-primary">Debug</span>""")
  }
  case object Info extends LogLevel {
    override def toHtml = Html("""<span class="label label-info">Info</span>""")
  }
  case object Warn extends LogLevel {
    override def toHtml = Html("""<span class="label label-warning">Warn</span>""")
  }
  case object Error extends LogLevel {
    override def toHtml = Html("""<span class="label label-danger">Error</span>""")
  }
  case object Fatal extends LogLevel {
    override def toHtml = Html("""<span class="label label-danger">Fatal</span>""")
  }
}
