package services.scalaexport

import better.files._

object ExportConfig {
  case class Result(
    projectName: String,
    classNames: Map[String, String],
    propertyNames: Map[String, String],
    packages: Map[String, String],
    searchColumns: Map[String, Seq[String]]
  )

  def emptyResult(k: String) = {
    val em = Map.empty[String, String]
    Result(k, em, em, em, Map.empty[String, Seq[String]])
  }

  def load(key: String) = {
    val f = s"./tmp/$key.txt".toFile
    if (f.exists) {
      loadConfig(key, f.lineIterator)
    } else {
      emptyResult(key)
    }
  }

  private[this] def loadConfig(key: String, lines: Iterator[String]) = {
    var currentSection = "unknown"
    var projectName = key
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
          case h :: t :: Nil => ExportHelper.toIdentifier(h.trim()) -> t.trim()
          case _ => throw new IllegalStateException(s"Invalid property line [$line].")
        }
        currentSection match {
          case "project" => prop._1 match {
            case "name" => projectName = prop._2
            case _ => throw new IllegalStateException(s"Unhandled project key [${prop._1}].")
          }
          case "classname" => classNames += prop
          case "propertyname" => propertyNames += prop
          case "package" => packages += prop
          case "searchcolumns" => searchColumns += ExportHelper.toIdentifier(prop._1) -> prop._2.split(",").map(_.trim)
          case _ => throw new IllegalStateException(s"Invalid section [$currentSection].")
        }
      } else {
        throw new IllegalStateException(s"Invalid line [$line].")
      }
    }

    Result(projectName, classNames, propertyNames, packages, searchColumns)
  }
}
