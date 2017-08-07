package services.scalaexport.config

import services.scalaexport.config.ExportConfig.Result
import better.files._

object ExportConfigWriter {
  def write(result: Result) = {
    val f = s"./tmp/${result.key}.txt".toFile
    val fileContents = new StringBuilder()
    def log(s: String = "") = fileContents.append(s + "\n")

    log("# Project settings")
    log("[project]")
    if (result.projectName.trim.nonEmpty) {
      log(s"name = ${result.projectName}")
      log()
    }
    result.projectLocation.foreach(l => log(s"location = $l"))

    if (result.classNames.nonEmpty) {
      log("# Transforms names of generated classes.")
      log("[classnames]")
      result.classNames.map { c =>
        log(s"${c._1} = ${c._2}")
      }
      log()
    }
    if (result.propertyNames.nonEmpty) {
      log("# Renames the provided \"table.column\".")
      log("[propertynames]")
      result.propertyNames.map { c =>
        log(s"${c._1} = ${c._2}")
      }
      log()
    }
    if (result.packages.nonEmpty) {
      log("# Moves provided tables in to a subpackage.")
      log("[packages]")
      result.packages.map { c =>
        log(s"${c._1} = ${c._2}")
      }
      log()
    }

    if (result.searchColumns.nonEmpty) {
      log("# Defines columns to search for a provided table name.")
      log("[searchcolumns]")
      result.searchColumns.map { c =>
        log(s"${c._1} = ${c._2.mkString(", ")}")
      }
      log()
    }

    f.overwrite(fileContents.toString)
  }
}
