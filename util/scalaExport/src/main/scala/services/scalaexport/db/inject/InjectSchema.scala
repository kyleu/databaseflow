package services.scalaexport.db.inject

import better.files.File
import models.scalaexport.db.ExportResult

object InjectSchema {
  def inject(result: ExportResult, rootDir: File) = {
    val models = result.models.filterNot(_.provided)

    def enumQueryFieldsFor(s: String) = {
      val newContent = if (result.config.enums.isEmpty) {
        "    Seq.empty[Field[GraphQLContext, Unit]]"
      } else {
        result.config.enums.map(e => s"    ${e.fullClassName}Schema.queryFields").sorted.mkString(" ++\n  ")
      }
      InjectHelper.replaceBetween(original = s, start = "    // Start enum query fields", end = s"    // End enum query fields", newContent = newContent)
    }

    def modelQueryFieldsFor(s: String) = {
      val newContent = models.map(m => s"    ${m.modelClass}Schema.queryFields").sorted.mkString(" ++\n  ")
      InjectHelper.replaceBetween(original = s, start = "    // Start model query fields", end = s"    // End model query fields", newContent = newContent)
    }

    def mutationFieldsFor(s: String) = {
      val newContent = models.filter(_.pkFields.nonEmpty).map { m =>
        s"    ${m.modelClass}Schema.mutationFields"
      }.sorted.mkString(" ++\n  ")
      InjectHelper.replaceBetween(
        original = s,
        start = "    // Start model mutation fields",
        end = s"    // End model mutation fields",
        newContent = newContent
      )
    }

    def fetcherFieldsFor(s: String) = if (result.getMarkers("fetcher").isEmpty) {
      s
    } else {
      val newContent = "    Seq(\n" + result.getMarkers("fetcher").sorted.map("      " + _.trim()).mkString(",\n") + "\n    )"
      InjectHelper.replaceBetween(original = s, start = "    // Start model fetchers", end = s"    // End model fetchers", newContent = newContent)
    }

    val schemaSourceFile = rootDir / "app" / "models" / "graphql" / "Schema.scala"
    val newContent = fetcherFieldsFor(enumQueryFieldsFor(modelQueryFieldsFor(mutationFieldsFor(schemaSourceFile.contentAsString))))
    schemaSourceFile.overwrite(newContent)

    "Schema.scala" -> newContent
  }
}
