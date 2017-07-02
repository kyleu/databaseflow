package models.scalaexport

case class ScalaFile(pkg: String, key: String) {
  private[this] val buf = new StringBuilder()
  buf.append(s"package $pkg\n\n")

  private[this] var currentIndent = 0

  val filename = pkg.replaceAllLiterally(".", "/") + "/" + key + ".scala"

  def add(line: String = "", indentDelta: Int = 0) = {
    if (indentDelta < 0) {
      currentIndent += indentDelta
    }
    val ws = (0 until currentIndent).map(_ => "  ").mkString
    if (indentDelta > 0) {
      currentIndent += indentDelta
    }

    buf.append(ws + line + "\n")
  }

  def render() = buf.toString
}
