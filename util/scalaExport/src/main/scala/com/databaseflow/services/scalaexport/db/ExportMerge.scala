package com.databaseflow.services.scalaexport.db

import better.files._
import com.databaseflow.models.scalaexport.file.OutputFile
import com.databaseflow.services.scalaexport.db.file.ReadmeFile

object ExportMerge {
  private[this] def projectNameReplacements(prop: String, cls: String, root: File) = {
    def fix(f: File) = {
      val c = f.contentAsString
        .replaceAllLiterally("boilerplay", prop)
        .replaceAllLiterally("boilerplay", prop)
        .replaceAllLiterally("Boilerplay", cls)
      f.overwrite(c)
    }

    fix(root / "deploy.yaml")
    fix(root / "app" / "util" / "web" / "LoggingFilter.scala")
    fix(root / "app" / "views" / "index.scala.html")
    fix(root / "app" / "views" / "layout" / "simple.scala.html")
    fix(root / "app" / "util" / "Logging.scala")
    fix(root / "app" / "util" / "metrics" / "Instrumented.scala")
    fix(root / "conf" / "application.conf")
    fix(root / "conf" / "logback.xml")
    fix(root / "public" / "manifest.json")
    fix(root / "project" / "Server.scala")
    fix(root / "project" / "Shared.scala")
    fix(root / "shared" / "src" / "main" / "scala" / "util" / "Config.scala")

    val cssFile = root / "app" / "assets" / "stylesheets" / "boilerplay.less"
    cssFile.moveTo(cssFile.parent / (prop + ".less"))
  }

  private[this] def getSrcDir(source: String, log: String => Unit) = {
    import scala.sys.process._

    log(s"Cloning boilerplay.")

    if (source == "boilerplay") {
      val dir = "./tmp/boilerplay".toFile
      if (!dir.exists) {
        "git clone https://github.com/KyleU/boilerplay.git ./tmp/boilerplay".!!
        (dir / ".git").delete()
      }
      dir
    } else {
      throw new IllegalStateException(s"Unsupported export source [$source].")
    }
  }

  def mergeDirectories(
    projectId: Option[String], projectTitle: String,
    coreDir: File, root: (File, Seq[OutputFile]), wiki: (File, Seq[OutputFile]),
    log: String => Unit, source: String = "boilerplay"
  ) = {
    if (root._1.exists) {
      log(s"Overwriting existing project at [${root._1.path}].")
    } else {
      log(s"Creating initial project at [${root._1.path}].")
      root._1.createDirectory()
      getSrcDir(source, log).copyTo(root._1)
      (root._1 / "license").delete(swallowIOExceptions = true)
      (root._1 / "readme.md").overwrite(ReadmeFile.content(projectTitle))
      projectId.foreach(id => projectNameReplacements(id, projectTitle, root._1))
    }

    val coreResults = ExportMergeHelper.writeCore(coreDir, log)
    val srcResults = ExportMergeHelper.writeSource(root._1, log)
    val rootResults = ExportMergeHelper.writeFiles(root._1, root._2, log)
    val wikiResults = ExportMergeHelper.writeFiles(wiki._1, wiki._2, log)

    log("Merge complete.")

    (coreResults ++ srcResults ++ rootResults ++ wikiResults).groupBy(x => x).map(x => x._1 -> x._2.size)
  }
}
