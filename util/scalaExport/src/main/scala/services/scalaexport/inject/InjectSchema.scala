package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectSchema {
  def inject(result: ExportResult, rootDir: File) = {
    val models = result.models.filterNot(_.provided)

    def queryFieldsFor(s: String) = {
      val newContent = models.map(m => s"    ${m.modelClass}Schema.queryFields").sorted.mkString(" ++\n  ")
      InjectHelper.replaceBetween(original = s, start = "    // Start model query fields", end = s"    // End model query fields", newContent = newContent)
    }

    def mutationFieldsFor(s: String) = {
      val newContent = models.filter(_.pkFields.nonEmpty).map { m =>
        s"    ${m.modelClass}Schema.mutationFields"
      }.sorted.mkString(" ++\n  ")
      InjectHelper.replaceBetween(original = s, start = "    // Start model mutation fields", end = s"    // End model mutation fields", newContent = newContent)
    }

    def fetcherFieldsFor(s: String) = if (result.getMarkers("fetcher").isEmpty) {
      s
    } else {
      val newContent = "    Seq(\n" + result.getMarkers("fetcher").sorted.map("      " + _.trim()).mkString(",\n") + "\n    )"
      InjectHelper.replaceBetween(original = s, start = "    // Start model fetchers", end = s"    // End model fetchers", newContent = newContent)
    }

    val schemaSourceFile = rootDir / "app" / "models" / "graphql" / "Schema.scala"
    val newContent = fetcherFieldsFor(queryFieldsFor(mutationFieldsFor(schemaSourceFile.contentAsString)))
    schemaSourceFile.overwrite(newContent)

    "Schema.scala" -> newContent
  }
}
