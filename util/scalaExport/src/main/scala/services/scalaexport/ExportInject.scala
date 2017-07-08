package services.scalaexport

import better.files._
import models.scalaexport.ExportResult

object ExportInject {
  def inject(result: ExportResult) = {
    val rootDir = s"./tmp/${result.id}".toFile

    val schemaSourceFile = rootDir / "app" / "models" / "graphql" / "Schema.scala"
    val schemaSourceContent = schemaSourceFile.contentAsString
    val newQueryFields = result.models.map(m => s" ++ \n    models.${(m._1 :+ m._2).mkString(".")}Schema.queryFields").mkString
    val schemaTargetContent = schemaSourceContent.replaceAllLiterally("queryFields // ++ others", s"queryFields$newQueryFields")
    schemaSourceFile.overwrite(schemaTargetContent)

    result.log("Injection complete.")
  }
}
