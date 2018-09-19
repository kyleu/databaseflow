package models.codegen

object Capabilities {
  case class SqlFunction(name: String, typ: String)
}

case class Capabilities(
    engine: Engine,
    columnTypes: Seq[(String, Option[String])] = Nil,
    functions: Seq[Capabilities.SqlFunction] = Nil
)
