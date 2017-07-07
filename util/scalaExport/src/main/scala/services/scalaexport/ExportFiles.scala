package services.scalaexport

import better.files._
import models.scalaexport.ExportResult

object ExportFiles {
  def persist(result: ExportResult) = {
    val rootDir = "./tmp/scalaexport".toFile
    if (rootDir.exists) { rootDir.delete() }
    rootDir.createDirectory()

    result.files.map { file =>
      val f = if (file._1.isEmpty) {
        rootDir / file._2
      } else {
        rootDir / file._1.mkString("/") / file._2
      }
      f.createIfNotExists(createParents = true)
      f.writeText(file._3)
    }
  }

  private[this] def projectNameReplacements(key: String, root: File) = {
    val className = ExportHelper.toScalaClassName.convert(key)
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

  def merge(key: String) = {
    val rootDir = s"./tmp/$key".toFile
    if (rootDir.exists) { rootDir.delete() }
    rootDir.createDirectory()

    "./tmp/boilerplay".toFile.copyTo(rootDir)
    projectNameReplacements(key, rootDir)

    "./tmp/scalaexport".toFile.copyTo(rootDir / "app")
  }
}
