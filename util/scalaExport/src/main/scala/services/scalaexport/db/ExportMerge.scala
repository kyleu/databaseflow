package services.scalaexport.db

import better.files._
import models.scalaexport.file.OutputFile
import services.scalaexport.ExportFiles
import services.scalaexport.db.file.ReadmeFile

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

  def merge(projectId: Option[String], projectTitle: String, rootDir: File, rootFiles: Seq[OutputFile], log: String => Unit, source: String = "boilerplay") = {
    if (rootDir.exists) {
      log(s"Overwriting existing project at [${rootDir.path}].")
    } else {
      log(s"Creating initial project at [${rootDir.path}].")
      rootDir.createDirectory()
      getSrcDir(source, log).copyTo(rootDir)
      (rootDir / "license").delete(swallowIOExceptions = true)
      (rootDir / "readme.md").overwrite(ReadmeFile.content(projectTitle))
      projectId.foreach(id => projectNameReplacements(id, projectTitle, rootDir))
    }

    val src = ExportFiles.rootLocation.toFile
    val srcResults = src.listRecursively.filter(_.isRegularFile).map { c =>
      val p = c.pathAsString.substring(c.pathAsString.indexOf("scalaexport") + 12)
      val tgt = rootDir / p
      if (tgt.exists) {
        val tgtContent = tgt.contentAsString
        if (!tgtContent.contains("Generated File")) {
          log(s"Skipping modified file [${tgt.pathAsString}].")
          "modified"
        } else if (tgtContent == c.contentAsString) {
          "same-content"
        } else {
          c.copyTo(tgt, overwrite = true)
          "overwrite"
        }
      } else {
        tgt.createIfNotExists(createParents = true)
        c.copyTo(tgt, overwrite = true)
        "create"
      }
    }

    val rootResults = rootFiles.map { rf =>
      val f = rootDir / rf.packageDir / rf.filename
      if (f.exists) {
        val tgtContent = f.contentAsString
        if (!tgtContent.contains("Generated File")) {
          log(s"Skipping modified root file [${f.pathAsString}].")
          "modified-root"
        } else if (tgtContent == rf.rendered) {
          // result.log(s"Skipping unchanged root file [${tgt.pathAsString}].")
          "same-content-root"
        } else {
          f.delete(swallowIOExceptions = true)
          f.createIfNotExists()
          f.writeText(rf.rendered)
          "overwrite-root"
        }
      } else {
        f.writeText(rf.rendered)
        "create-root"
      }
    }

    log("Merge complete.")

    (srcResults.toSeq ++ rootResults).groupBy(x => x).map(x => x._1 -> x._2.size)
  }
}
