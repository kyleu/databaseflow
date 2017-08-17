package services.scalaexport.config

import enumeratum._

sealed trait ExportEngine extends EnumEntry {
  def key: String
  def title: String
  override def toString = key
}

object ExportEngine extends Enum[ExportEngine] {
  case object MySQL extends ExportEngine {
    override val key = "mysql"
    override val title = "MySQL"
  }

  case object PostgreSQL extends ExportEngine {
    override val key = "postgres"
    override val title = "PostgreSQL"
  }

  override val values = findValues
}
