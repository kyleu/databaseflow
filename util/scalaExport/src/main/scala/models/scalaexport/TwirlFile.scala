package models.scalaexport

case class TwirlFile(override val pkg: Seq[String], override val key: String) extends OutputFile(pkg, key, key + ".scala.html")
