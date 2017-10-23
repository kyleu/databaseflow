package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectSearch {
  def inject(result: ExportResult, rootDir: File) = {
    def searchFieldsFor(s: String) = {
      val withStrings = searchStringFieldsFor(s)
      val withInts = searchIntFieldsFor(withStrings)
      searchUuidFieldsFor(withInts)
    }

    def searchUuidFieldsFor(s: String) = {
      val uuidModels = result.getMarkers("uuid-search").map(s => InjectSearchParams.fromString(result, s))

      if (uuidModels.isEmpty) {
        s
      } else {
        val uuidFields = uuidModels.map { m =>
          s"    val ${m.model.propertyName} = ${m.model.serviceReference}.getByPrimaryKey(u, id).map { model =>\n" +
            s"      ${m.viewClass}(model, ${m.message})\n" +
            "    }.toSeq"
        }
        val uuidFutures = uuidModels.map(_.model.propertyName).mkString(", ")
        val newContent = uuidFields.sorted.mkString("\n") + s"\n\n    val uuidSearches = Seq[Seq[Html]]($uuidFutures)"
        InjectHelper.replaceBetween(original = s, start = "    // Start uuid searches", end = "    // End uuid searches", newContent = newContent)
      }
    }

    def searchIntFieldsFor(s: String) = {
      val intModels = result.getMarkers("int-search").map(s => InjectSearchParams.fromString(result, s))

      if (intModels.isEmpty) {
        s
      } else {
        val intFields = intModels.map { m =>
          s"    val ${m.model.propertyName} = ${m.model.serviceReference}.getByPrimaryKey(u, id).map { model =>\n" ++
            s"      ${m.viewClass}(model, ${m.message})\n" +
            "    }.toSeq"
        }
        val intFutures = intModels.map(_.model.propertyName).mkString(", ")
        val newContent = intFields.sorted.mkString("\n") + s"\n\n    val intSearches = Seq[Seq[Html]]($intFutures)"
        InjectHelper.replaceBetween(original = s, start = "    // Start int searches", end = "    // End int searches", newContent = newContent)
      }
    }

    def searchStringFieldsFor(s: String) = {
      val stringModels = result.getMarkers("string-search").map(s => InjectSearchParams.fromString(result, s))

      if (stringModels.isEmpty) {
        s
      } else {
        val stringFields = stringModels.map { m =>
          s"    val ${m.model.propertyName} = ${m.model.serviceReference}.searchExact(u, q = q, limit = Some(5)).map { model =>\n" ++
            s"      ${m.viewClass}(model, ${m.message})\n" +
            "    }"
        }
        val stringFutures = stringModels.map(_.model.propertyName).mkString(", ")
        val newContent = stringFields.sorted.mkString("\n") + s"\n\n    val stringSearches = Seq[Seq[Html]]($stringFutures)"
        InjectHelper.replaceBetween(original = s, start = "    // Start string searches", end = "    // End string searches", newContent = newContent)
      }
    }

    val searchSourceFile = rootDir / "app" / "controllers" / "admin" / "system" / "SearchController.scala"

    val newContent = searchFieldsFor(searchSourceFile.contentAsString)
    searchSourceFile.overwrite(newContent)

    "SearchController.scala" -> newContent
  }
}
