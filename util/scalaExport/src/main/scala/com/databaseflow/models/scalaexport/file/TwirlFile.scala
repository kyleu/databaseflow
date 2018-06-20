package com.databaseflow.models.scalaexport.file

case class TwirlFile(
    override val pkg: Seq[String], override val key: String, override val core: Boolean = false
) extends OutputFile("app", pkg, key, key + ".scala.html", core) {
  override def prefix = "@* Generated File *@\n"
}
