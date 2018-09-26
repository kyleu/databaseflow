package com.databaseflow.services.scalaexport.db

import better.files._
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration

object ExportValidation {
  def validate(config: ExportConfiguration, result: ScalaExportService.Result) = {
    val out = result.er.files.map(_.path.stripPrefix("./"))
    val corePrefix = config.rootDir.relativize(config.coreDir).toString
    val wikiPrefix = config.rootDir.relativize(config.wikiDir).toString
    val files = (getGeneratedFiles(config.coreDir) ++ getGeneratedFiles(config.rootDir) ++ getGeneratedFiles(config.wikiDir)).distinct.map { f =>
      val s = config.rootDir.relativize(f.path).toString.stripPrefix("test/").stripPrefix(corePrefix).stripPrefix(wikiPrefix).stripPrefix("/")
      f -> clean(clean(s, config.wikiLocation), config.coreLocation)
    }
    files.flatMap { f =>
      if (out.contains(f._2)) {
        None
      } else {
        f._1.delete()
        Some(f._2 -> "Untracked")
      }
    }
  }

  private[this] val badBoys = Set("target", "public", ".idea", ".git", "project")
  private[this] def clean(s: String, x: Option[String]) = x match {
    case Some(xx) => s.stripPrefix(xx)
    case None => s
  }

  private[this] def getGeneratedFiles(f: File): Seq[File] = {
    if (!f.isDirectory) { throw new IllegalStateException(s"[$f] is not a directory.") }
    f.children.toSeq.flatMap {
      case child if badBoys(child.name) => Nil
      case child if child.isDirectory => getGeneratedFiles(child)
      case child if child.contentAsString.contains("Generated File") => Seq(child)
      case child => Nil
    }
  }
}
