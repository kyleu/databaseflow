package com.databaseflow.models.scalaexport.file

case class MarkdownFile(override val pkg: Seq[String], override val key: String) extends OutputFile(
  dir = ".", pkg = pkg, key = key, filename = key + ".md"
) {
  override def prefix = "<!-- Generated File -->\n"

  def addHeader(s: String, level: Int = 1) = {
    add(s"${(0 until level).map(_ => "#").mkString} $s")
    add()
  }

  def addScala(lines: String*) = {
    add("```scala")
    lines.foreach(s => add(s))
    add("```")
  }
}
