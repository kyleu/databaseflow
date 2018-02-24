package services.scalaexport

import better.files._
import models.scalaexport.ExportResult
import services.scalaexport.file.ReadmeFile

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

  private[this] def getSrcDir(result: ExportResult) = {
    import scala.sys.process._

    result.log(s"Cloning boilerplay.")

    if (result.config.source == "boilerplay") {
      val dir = "./tmp/boilerplay".toFile
      if (!dir.exists) {
        "git clone https://github.com/KyleU/boilerplay.git ./tmp/boilerplay".!!
        (dir / ".git").delete()
      }
      dir
    } else {
      throw new IllegalStateException(s"Unsupported export source [${result.config.source}].")
    }
  }

  def merge(result: ExportResult, rootDir: File) = {
    if (rootDir.exists) {
      result.log("Overwriting existing project.")
    } else {
      result.log("Creating initial project.")
      rootDir.createDirectory()
      getSrcDir(result).copyTo(rootDir)
      (rootDir / "license").delete(swallowIOExceptions = true)
      (rootDir / "readme.md").overwrite(ReadmeFile.content(result.config.projectTitle))
      projectNameReplacements(result.config.projectId, result.config.projectTitle, rootDir)
    }

    val src = "./tmp/scalaexport".toFile
    src.listRecursively.filter(_.isRegularFile).foreach { c =>
      val p = c.pathAsString.substring(c.pathAsString.indexOf("scalaexport") + 12)
      val tgt = rootDir / p
      if (tgt.exists) {
        val tgtContent = tgt.contentAsString
        if (!tgtContent.contains(" Generated File")) {
          result.log(s"Skipping modified file [${tgt.pathAsString}].")
        } else if (tgtContent == c.contentAsString) {
          // noop
          // result.log(s"Skipping unchanged file [${tgt.pathAsString}].")
        } else {
          c.copyTo(tgt, overwrite = true)
        }
      } else {
        c.copyTo(tgt)
      }
    }

    result.rootFiles.foreach { rf =>
      val f = rootDir / rf.packageDir / rf.filename
      if (f.exists && f.contentAsString.indexOf(" Generated File") == -1) {
        result.log(s"Skipping modified file [${f.pathAsString}].")
      } else {
        f.delete(swallowIOExceptions = true)
        f.createIfNotExists()
        f.writeText(rf.rendered)
      }
    }

    result.log("Merge complete.")
  }
}
