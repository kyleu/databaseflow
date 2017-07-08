package services.scalaexport

import better.files._
import models.scalaexport.ExportResult

object ExportMerge {
  private[this] def projectNameReplacements(key: String, root: File) = {
    val className = ExportHelper.toClassName(key)
    def fix(f: File) = f.overwrite(f.contentAsString.replaceAllLiterally("boilerplay", key).replaceAllLiterally("Boilerplay", className))

    fix(root / "app" / "utils" / "web" / "LoggingFilter.scala")
    fix(root / "app" / "views" / "index.scala.html")
    fix(root / "app" / "views" / "layout" / "simple.scala.html")
    fix(root / "client" / "src" / "main" / "scala" / "NetworkHelper.scala")
    fix(root / "client" / "src" / "main" / "scala" / "ResponseMessageHelper.scala")
    fix(root / "conf" / "application.conf")
    fix(root / "conf" / "logback.xml")
    fix(root / "public" / "manifest.json")
    fix(root / "project" / "Server.scala")
    fix(root / "project" / "Shared.scala")
    fix(root / "shared" / "src" / "main" / "scala" / "utils" / "Config.scala")
    fix(root / "util" / "metrics" / "src" / "main" / "scala" / "utils" / "Logging.scala")
    fix(root / "util" / "metrics" / "src" / "main" / "scala" / "utils" / "metrics" / "Checked.scala")
    fix(root / "util" / "metrics" / "src" / "main" / "scala" / "utils" / "metrics" / "Instrumented.scala")

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

  def merge(result: ExportResult) = {
    val rootDir = s"./tmp/${result.id}".toFile
    if (rootDir.exists) { rootDir.delete() }
    rootDir.createDirectory()

    getSrcDir(result).copyTo(rootDir)
    projectNameReplacements(result.id, rootDir)

    "./tmp/scalaexport".toFile.copyTo(rootDir / "app")

    result.log("Merge complete.")
  }
}
