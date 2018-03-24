package services.scalaexport.db.inject

import better.files.File
import models.scalaexport.db.ExportResult

object InjectOpenApiPaths {
  def inject(result: ExportResult, rootDir: File) = {
    val models = result.models.filterNot(_.provided).sortBy(x => x.pkg.mkString("/") + "/" + x.propertyName)

    def enumPathsFor(s: String) = {
      val newContent = if (result.config.enums.isEmpty) {
        ""
      } else {
        result.config.enums.map { e =>
          s"""  "#include:${e.pkg.mkString("/")}/${e.propertyName}.json": "*","""
        }.sorted.mkString("\n")
      }
      InjectHelper.replaceBetween(original = s, start = "  // Start enum paths", end = s"  // End enum paths", newContent = newContent)
    }

    def modelPathsFor(s: String) = {
      val newContent = models.map { m =>
        val comma = if (models.lastOption.contains(m)) { "" } else { "," }
        s"""  "#include:${m.pkg.mkString("/")}/${m.propertyName}.json": "*"$comma"""
      }.sorted.mkString("\n")
      InjectHelper.replaceBetween(original = s, start = "  // Start schema paths", end = s"  // End schema paths", newContent = newContent)
    }

    val schemaSourceFile = rootDir / "conf" / "openapi" / "paths" / "paths.json"
    val newContent = enumPathsFor(modelPathsFor(schemaSourceFile.contentAsString))
    schemaSourceFile.overwrite(newContent)

    "paths.json" -> newContent
  }
}
