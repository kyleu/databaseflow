package com.databaseflow.services.scalaexport.db.inject

import better.files.File
import com.databaseflow.models.scalaexport.db.ExportResult

object InjectOpenApiSchema {
  def inject(result: ExportResult, rootDir: File) = {
    val models = result.models.filterNot(_.provided).sortBy(x => x.pkg.mkString("/") + "/" + x.propertyName)

    def modelSchemasFor(s: String) = {
      val newContent = models.map { m =>
        val comma = if (models.lastOption.contains(m)) { "" } else { "," }
        s"""  "#include:${m.pkg.mkString("/")}/${m.propertyName}.json": "*"$comma"""
      }.sorted.mkString("\n")
      InjectHelper.replaceBetween(original = s, start = "  // Start model schemas", end = s"  // End model schemas", newContent = newContent)
    }

    val schemaSourceFile = rootDir / "conf" / "openapi" / "components" / "schema" / "schemas.json"
    val newContent = modelSchemasFor(schemaSourceFile.contentAsString)
    schemaSourceFile.overwrite(newContent)

    "paths.json" -> newContent
  }
}
