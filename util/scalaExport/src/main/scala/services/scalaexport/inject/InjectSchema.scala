package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectSchema {
  def inject(result: ExportResult, rootDir: File) = {
    def queryFieldsFor(s: String) = {
      val newContent = result.models.map(m => s"    models.${(m._1 :+ m._2).mkString(".")}Schema.queryFields").sorted.mkString(" ++\n  ")
      InjectHelper.replaceBetween(original = s, start = "    // Start model query fields", end = s"    // End model query fields", newContent = newContent)
    }

    def mutationFieldsFor(s: String) = {
      val newContent = result.models.map(m => s" ++\n    models.${(m._1 :+ m._2).mkString(".")}Schema.mutationFields").sorted.mkString
      InjectHelper.replaceBetween(original = s, start = "    // Start model mutation fields", end = s"    // End model mutation fields", newContent = newContent)
    }

    def fetcherFieldsFor(s: String) = if (result.getMarkers("fetcher").isEmpty) {
      s
    } else {
      val newContent = "    Seq(\n" + result.getMarkers("fetcher").sorted.map("      " + _.trim()).mkString(",\n") + "\n    )"
      InjectHelper.replaceBetween(original = s, start = "    // Start model fetchers", end = s"    // End model fetchers", newContent = newContent)
    }

    val schemaSourceFile = rootDir / "app" / "models" / "graphql" / "Schema.scala"
    val newContent = fetcherFieldsFor(queryFieldsFor(schemaSourceFile.contentAsString))
    schemaSourceFile.overwrite(newContent)

    "Schema.scala" -> newContent
  }
}
