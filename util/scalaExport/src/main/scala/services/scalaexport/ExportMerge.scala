package services.scalaexport

import better.files.File.CopyOptions
import better.files._
import models.scalaexport.ExportResult

object ExportMerge {
  private[this] def projectNameReplacements(id: String, root: File) = {
    val key = ExportHelper.toIdentifier(id)
    val className = ExportHelper.toClassName(id)
    def fix(f: File) = f.overwrite(f.contentAsString.replaceAllLiterally("boilerplay", key).replaceAllLiterally("Boilerplay", className))

    fix(root / "app" / "util" / "web" / "LoggingFilter.scala")
    fix(root / "app" / "views" / "index.scala.html")
    fix(root / "app" / "views" / "layout" / "simple.scala.html")
    fix(root / "client" / "src" / "main" / "scala" / "NetworkHelper.scala")
    fix(root / "client" / "src" / "main" / "scala" / "ResponseMessageHelper.scala")
    fix(root / "conf" / "application.conf")
    fix(root / "conf" / "logback.xml")
    fix(root / "public" / "manifest.json")
    fix(root / "project" / "Server.scala")
    fix(root / "project" / "Shared.scala")
    fix(root / "shared" / "src" / "main" / "scala" / "util" / "Config.scala")
    fix(root / "util" / "metrics" / "src" / "main" / "scala" / "util" / "Logging.scala")
    fix(root / "util" / "metrics" / "src" / "main" / "scala" / "util" / "metrics" / "Checked.scala")
    fix(root / "util" / "metrics" / "src" / "main" / "scala" / "util" / "metrics" / "Instrumented.scala")

    val jsFile = root / "client" / "src" / "main" / "scala" / "Boilerplay.scala"
    fix(jsFile)
    jsFile.moveTo(jsFile.parent / (className + ".scala"))

    val cssFile = root / "app" / "assets" / "stylesheets" / "boilerplay.less"
    cssFile.moveTo(cssFile.parent / (key + ".less"))
  }

  private[this] def getSrcDir(result: ExportResult) = {
    val dir = "./tmp/boilerplay".toFile
    if (!dir.exists) {
      import scala.sys.process._

      result.log("Cloning boilerplay.")
      "git clone https://github.com/KyleU/boilerplay.git ./tmp/boilerplay".!!

      result.log("Deleting boilerplay git history.")
      (dir / ".git").delete()
    }
    dir
  }

  def merge(result: ExportResult, rootDir: File) = {
    if (rootDir.exists) {
      result.log("Overwriting existing project.")
    } else {
      result.log("Creating initial project.")
      rootDir.createDirectory()
      getSrcDir(result).copyTo(rootDir)
      (rootDir / "license").delete(swallowIOExceptions = true)
      projectNameReplacements(result.id, rootDir)
    }

    "./tmp/scalaexport".toFile.copyTo(rootDir / "app")(CopyOptions(true))

    result.log("Merge complete.")
  }
}
