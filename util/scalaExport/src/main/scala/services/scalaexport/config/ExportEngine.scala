package services.scalaexport.config

import enumeratum._

sealed trait ExportEngine extends EnumEntry {
  def key: String
  def title: String
  def lQuote: String
  def rQuote: String = lQuote
  def lQuoteEscaped: String = lQuote
  def rQuoteEscaped: String = rQuote

  override def toString = key
}

object ExportEngine extends Enum[ExportEngine] {
  case object MySQL extends ExportEngine {
    override val key = "mysql"
    override val title = "MySQL"
    override val lQuote = "`"
  }

  case object PostgreSQL extends ExportEngine {
    override val key = "postgres"
    override val title = "PostgreSQL"
    override val lQuote = "\""
    override val lQuoteEscaped = "\\" + lQuote
    override val rQuoteEscaped = "\\" + rQuote
  }

  override val values = findValues
}
