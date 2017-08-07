package services.scalaexport.config

import services.scalaexport.config.ExportConfig.Result
import better.files._
import services.scalaexport.ExportHelper

object ExportConfigReader {
  def read(key: String) = {
    val f = s"./tmp/$key.txt".toFile
    if (f.exists) {
      loadConfig(key, f.lineIterator)
    } else {
      val em = Map.empty[String, String]
      val emSeq = Map.empty[String, Seq[String]]
      Result(key, key, None, em, em, em, em, em, emSeq).withDefaults
    }
  }

  private[this] def loadConfig(key: String, lines: Iterator[String]) = {
    var currentSection = "unknown"
    var projectName = key
    var projectLocation: Option[String] = None

    val em = Map.empty[String, String]
    val emSeq = Map.empty[String, Seq[String]]

    var ignored = em
    var classNames = em
    var extendModels = em
    var propertyNames = em
    var packages = em
    var searchColumns = emSeq
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
            case "location" => projectLocation = Some(prop._2)
            case _ => throw new IllegalStateException(s"Unhandled project key [${prop._1}].")
          }
          case "ignored" => ignored += prop
          case "classnames" => classNames += prop
          case "extendmodels" => extendModels += prop
          case "propertynames" => propertyNames += prop
          case "packages" => packages += prop
          case "searchcolumns" => searchColumns += ExportHelper.toIdentifier(prop._1) -> prop._2.split(",").map(_.trim)
          case _ => throw new IllegalStateException(s"Invalid section [$currentSection].")
        }
      } else {
        throw new IllegalStateException(s"Invalid line [$line].")
      }
    }

    Result(key, projectName, projectLocation, ignored, classNames, extendModels, propertyNames, packages, searchColumns).withDefaults
  }
}
