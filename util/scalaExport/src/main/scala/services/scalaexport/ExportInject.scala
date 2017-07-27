package services.scalaexport

import better.files._
import models.scalaexport.ExportResult

object ExportInject {
  def inject(result: ExportResult) = {
    val rootDir = s"./tmp/${ExportHelper.toIdentifier(result.id)}".toFile

    val s = updateSchemaFile(result, rootDir)
    val i = updateIconsFile(result, rootDir)

    result.log("Injection complete.")
    Seq(s, i)
  }

  private[this] def updateSchemaFile(result: ExportResult, rootDir: File) = {
    def queryFieldsFor(s: String) = {
      val newContent = result.models.map(m => s" ++\n    models.${(m._1 :+ m._2).mkString(".")}Schema.queryFields").sorted.mkString
      s.replaceAllLiterally("queryFields // ++ others", s"queryFields$newContent")
    }

    def fetcherFieldsFor(s: String) = if (result.getMarkers("fetcher").isEmpty) {
      s
    } else {
      val newContent = result.getMarkers("fetcher").sorted.mkString(",\n    ")
      s.replaceAllLiterally("fetchers()", s"fetchers(\n    $newContent\n  )")
    }

    val schemaSourceFile = rootDir / "app" / "models" / "graphql" / "Schema.scala"
    val newContent = fetcherFieldsFor(queryFieldsFor(schemaSourceFile.contentAsString))
    schemaSourceFile.overwrite(newContent)

    "Schema.scala" -> newContent
  }

  private[this] def updateIconsFile(result: ExportResult, rootDir: File) = {
    def iconFieldsFor(s: String) = {
      val newContent = result.models.map(m => s"""\n  val ${ExportHelper.toIdentifier(m._2)} = "fa-folder-o"""").sorted.mkString
      s.replaceAllLiterally("val user = \"fa-user\"", s"""val user = "fa-user"\n\n  //Model Icons$newContent""")
    }

    val iconSourceFile = rootDir / "shared" / "src" / "main" / "scala" / "models" / "template" / "Icons.scala"
    val newContent = iconFieldsFor(iconSourceFile.contentAsString)
    iconSourceFile.overwrite(newContent)

    "Icons.scala" -> newContent
  }
}
