package models.codegen

import enumeratum._

sealed abstract class Engine(val id: String, val enabled: Boolean = true) extends EnumEntry {
  override def toString = id
}

object Engine extends Enum[Engine] {
  case object H2 extends Engine(id = "h2")
  case object MySQL extends Engine(id = "mysql")
  case object Oracle extends Engine(id = "oracle")
  case object PostgreSQL extends Engine(id = "postgres")
  case object SQLite extends Engine(id = "sqlite")
  case object SQLServer extends Engine(id = "sqlserver")

  override val values = findValues
}
