package models.scalaexport

case class RoutesFile(override val key: String) extends OutputFile(Seq("conf"), key, key + ".routes")
