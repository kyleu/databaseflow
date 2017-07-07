package services.scalaexport

import better.files._

object ExportConfig {
  def load(key: String) = {
    val f = s"./tmp/$key.txt".toFile
    if (f.exists) {
      loadConfig(f.lineIterator)
    } else {
      (Map.empty[String, String], Map.empty[String, String])
    }
  }

  private[this] def loadConfig(lines: Iterator[String]) = {
    var currentSection = "unknown"
    var classNames = Map.empty[String, String]
    var packages = Map.empty[String, String]

    lines.filterNot(_.trim.isEmpty).foreach { line =>
      if (line.startsWith("[")) {
        currentSection = line.replaceAllLiterally("[", "").replaceAllLiterally("]", "")
      } else if (line.contains('=')) {
        val prop = line.split("=").toList match {
          case h :: t :: Nil => h.trim() -> t.trim()
          case _ => throw new IllegalStateException(s"Invalid property line [$line].")
        }
        currentSection match {
          case "classname" => classNames += prop
          case "package" => packages += prop
          case _ => throw new IllegalStateException(s"Invalid section [$currentSection].")
        }
      } else {
        throw new IllegalStateException(s"Invalid line [$line].")
      }
    }

    (classNames, packages)
  }
}
