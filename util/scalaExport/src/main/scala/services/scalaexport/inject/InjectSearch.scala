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
          s"    val ${m.model.propertyName}F = ${m.model.serviceClass}.getByPrimaryKey(id).map { modelOpt =>\n" +
            s"      modelOpt.map(model => ${m.viewClass}(model, ${m.message})).toSeq\n" +
            "    }"
        }
        val uuidFutures = uuidModels.map(_.model.propertyName + "F")
        val newContent = uuidFields.sorted.mkString("\n") + s"\n\n    val uuidSearches = Seq[Future[Seq[Html]]](${uuidFutures.mkString(", ")})"
        InjectHelper.replaceBetween(original = s, start = "    // Start uuid searches", end = "    // End uuid searches", newContent = newContent)
      }
    }

    def searchIntFieldsFor(s: String) = {
      val intModels = result.getMarkers("int-search").map(s => InjectSearchParams.fromString(result, s))

      if (intModels.isEmpty) {
        s
      } else {
        val intFields = intModels.map { m =>
          s"    val ${m.model.propertyName}F = ${m.model.serviceClass}.getByPrimaryKey(id).map { modelOpt =>\n" ++
            s"      modelOpt.map(model => ${m.viewClass}(model, ${m.message})).toSeq\n" +
            "    }"
        }
        val intFutures = intModels.map(_.model.propertyName + "F")
        val newContent = intFields.sorted.mkString("\n") + s"\n\n    val intSearches = Seq[Future[Seq[Html]]](${intFutures.mkString(", ")})"
        InjectHelper.replaceBetween(original = s, start = "    // Start int searches", end = "    // End int searches", newContent = newContent)
      }
    }

    def searchStringFieldsFor(s: String) = {
      val stringModels = result.getMarkers("string-search").map(s => InjectSearchParams.fromString(result, s))

      if (stringModels.isEmpty) {
        s
      } else {
        val stringFields = stringModels.map { m =>
          s"    val ${m.model.propertyName}F = ${m.model.serviceClass}.searchExact(q = q, orderBys = Nil, limit = Some(5), offset = None).map { models =>\n" ++
            s"      models.map(model => ${m.viewClass}(model, ${m.message}))\n" +
            "    }"
        }
        val stringFutures = stringModels.map(_.model.propertyName + "F")
        val newContent = stringFields.sorted.mkString("\n") + s"\n\n    val stringSearches = Seq[Future[Seq[Html]]](${stringFutures.mkString(", ")})"
        InjectHelper.replaceBetween(original = s, start = "    // Start string searches", end = "    // End string searches", newContent = newContent)
      }
    }

    val searchSourceFile = rootDir / "app" / "controllers" / "admin" / "system" / "SearchController.scala"

    val newContent = searchFieldsFor(searchSourceFile.contentAsString)
    searchSourceFile.overwrite(newContent)

    "SearchController.scala" -> newContent
  }
}
