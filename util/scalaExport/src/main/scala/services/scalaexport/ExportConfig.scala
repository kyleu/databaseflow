package services.scalaexport

import better.files._

object ExportConfig {
  case class Result(
    classNames: Map[String, String],
    propertyNames: Map[String, String],
    packages: Map[String, String],
    searchColumns: Map[String, Seq[String]]
  )

  val emptyResult = {
    val em = Map.empty[String, String]
    Result(em, em, em, Map.empty[String, Seq[String]])
  }

  def load(key: String) = {
    val f = s"./tmp/$key.txt".toFile
    if (f.exists) {
      loadConfig(f.lineIterator)
    } else {
      emptyResult
    }
  }

  private[this] def loadConfig(lines: Iterator[String]) = {
    var currentSection = "unknown"
    var classNames = Map.empty[String, String]
    var propertyNames = Map.empty[String, String]
    var packages = Map.empty[String, String]
    var searchColumns = Map.empty[String, Seq[String]]
    lines.filterNot(_.trim.isEmpty).foreach { line =>
      if (line.startsWith("[")) {
        currentSection = line.replaceAllLiterally("[", "").replaceAllLiterally("]", "")
      } else if (line.startsWith("#")) {
        // Comment, noop.
      } else if (line.contains('=')) {
        val prop = line.split("=").toList match {
          case h :: t :: Nil => h.trim() -> t.trim()
          case _ => throw new IllegalStateException(s"Invalid property line [$line].")
        }
        currentSection match {
          case "classname" => classNames += prop
          case "propertyname" => propertyNames += prop
          case "package" => packages += prop
          case "searchcolumns" => searchColumns += prop._1 -> prop._2.split(",").map(_.trim)
          case _ => throw new IllegalStateException(s"Invalid section [$currentSection].")
        }
      } else {
        throw new IllegalStateException(s"Invalid line [$line].")
      }
    }

    Result(classNames, propertyNames, packages, searchColumns)
  }
}
