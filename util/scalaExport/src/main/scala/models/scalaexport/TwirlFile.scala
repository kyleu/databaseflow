package models.scalaexport

case class TwirlFile(override val pkg: Seq[String], override val key: String) extends OutputFile("app", pkg, key, key + ".scala.html")
