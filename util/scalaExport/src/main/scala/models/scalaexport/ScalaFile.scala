package models.scalaexport

case class ScalaFile(pkg: Seq[String], key: String) {
  private[this] var currentIndent = 0
  private[this] var imports = Set.empty[(String, String)]
  private[this] val lines = new StringBuilder()

  val filename = key + ".scala"

  def addImport(p: String, c: String) = imports += (p -> c)

  def add(line: String = "", indentDelta: Int = 0) = {
    if (indentDelta < 0) {
      currentIndent += indentDelta
    }
    val ws = (0 until currentIndent).map(_ => "  ").mkString
    if (indentDelta > 0) {
      currentIndent += indentDelta
    }

    lines.append(ws + line + "\n")
  }

  def render() = {
    val pkgString = s"package ${pkg.mkString(".")}\n\n"

    val impString = if (imports.isEmpty) {
      ""
    } else {
      imports.toSeq.groupBy(_._1).mapValues(_.map(_._2)).toList.sortBy(_._1).map { i =>
        i._2.size match {
          case 1 => s"import ${i._1}.${i._2.head}"
          case _ => s"import ${i._1}.{ ${i._2.sorted.mkString(", ")} }"
        }
      }.mkString("\n") + "\n\n"
    }

    pkgString + impString + lines.toString
  }
}
