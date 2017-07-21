package services.scalaexport

import better.files._
import models.scalaexport.ExportResult

object ExportInject {
  def inject(result: ExportResult) = {
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

    val rootDir = s"./tmp/${ExportHelper.toIdentifier(result.id)}".toFile

    val schemaSourceFile = rootDir / "app" / "models" / "graphql" / "Schema.scala"
    val newContent = fetcherFieldsFor(queryFieldsFor(schemaSourceFile.contentAsString))
    schemaSourceFile.overwrite(newContent)

    result.log("Injection complete.")

    Seq("Schema.scala" -> newContent)
  }
}
