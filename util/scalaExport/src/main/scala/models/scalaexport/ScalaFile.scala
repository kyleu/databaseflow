package models.scalaexport

case class ScalaFile(pkg: Seq[String], key: String) {
  private[this] var hasRendered = false
  private[this] var currentIndent = 0
  private[this] var imports = Set.empty[(String, String)]
  private[this] val lines = collection.mutable.ArrayBuffer.empty[String]

  private[this] val markers = collection.mutable.HashMap.empty[String, Seq[String]]
  def markersFor(key: String) = markers.getOrElseUpdate(key, Nil)
  def addMarker(key: String, v: String) = markers(key) = markersFor(key) :+ v

  val filename = key + ".scala"

  def addImport(p: String, c: String) = imports += (p -> c)

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

  lazy val rendered = {
    val pkgString = s"package ${pkg.mkString(".")}\n\n"

    val impString = if (imports.isEmpty) {
      ""
    } else {
      imports.toSeq.groupBy(_._1).mapValues(_.map(_._2)).toList.sortBy(_._1).map { i =>
        i._2.size match {
          case 1 => s"import ${i._1}.${i._2.head}"
          case _ => s"import ${i._1}.{${i._2.sorted.mkString(", ")}}"
        }
      }.mkString("\n") + "\n\n"
    }

    hasRendered = true

    pkgString + impString + lines.mkString
  }
}
