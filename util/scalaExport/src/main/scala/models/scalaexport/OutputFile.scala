package models.scalaexport

abstract class OutputFile(val dir: String, val pkg: Seq[String], val key: String, val filename: String) {
  private[this] var hasRendered = false
  private[this] var currentIndent = 0
  private[this] val lines = collection.mutable.ArrayBuffer.empty[String]

  private[this] val markers = collection.mutable.HashMap.empty[String, Seq[String]]
  def markersFor(key: String) = markers.getOrElseUpdate(key, Nil)
  def addMarker(key: String, v: String) = markers(key) = markersFor(key) :+ v

  def add(line: String = "", indentDelta: Int = 0): Unit = {
    if (hasRendered) {
      throw new IllegalStateException("Already rendered.")
    }
    if (indentDelta < 0) {
      currentIndent += indentDelta
    }
    val ws = if (line.trim.isEmpty) { "" } else { (0 until currentIndent).map(_ => "  ").mkString }
    if (indentDelta > 0) {
      currentIndent += indentDelta
    }

    lines += (ws + line + "\n")
  }

  def prefix: String = ""
  def suffix: String = ""

  lazy val rendered = {
    hasRendered = true
    prefix + lines.mkString + suffix
  }
}
