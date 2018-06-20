package com.databaseflow.services.scalaexport.db.inject

import better.files.File
import com.databaseflow.models.scalaexport.db.ExportResult

object InjectThrift {
  def inject(result: ExportResult, rootDir: File) = {
    Seq(injectKey("model", result, rootDir), injectKey("service", result, rootDir))
  }

  private[this] def injectKey(key: String, result: ExportResult, rootDir: File) = {
    def thriftFieldsFor(s: String) = {
      val startString = s"/* Begin generated Thrift $key includes */"
      val startIndex = s.indexOf(startString)
      val newContent = result.models.filterNot(m => m.ignored || m.provided).map {
        case m if key == "model" => s"""include "${("models" +: m.pkg).mkString("/")}/${m.className}.thrift""""
        case m if key == "service" => s"""include "${("services" +: m.pkg).mkString("/")}/${m.className}Service.thrift""""
        case _ => throw new IllegalStateException(s"Invalid key [$key].")
      }.sorted.mkString("\n")
      InjectHelper.replaceBetween(original = s, start = startString, end = s"/* End generated Thrift $key includes */", newContent = newContent)
    }

    val thriftSourceFile = rootDir / "doc" / "src" / "main" / "thrift" / s"${key}s.thrift"
    val newContent = thriftFieldsFor(thriftSourceFile.contentAsString)
    thriftSourceFile.overwrite(newContent)

    s"$key.thrift" -> newContent
  }
}
