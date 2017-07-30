package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectSchema {
  def inject(result: ExportResult, rootDir: File) = {
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
}
