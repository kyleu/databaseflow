package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

object InjectThriftServices {
  def inject(result: ExportResult, rootDir: File) = {
    def thriftFieldsFor(s: String) = {
      val startString = "/* Begin generated Thrift service includes */"
      val startIndex = s.indexOf(startString)
      val newContent = result.models.map { m =>
        s"""import "xxx.thrift""""
      }.sorted.mkString("\n")
      InjectHelper.replaceBetween(original = s, start = startString, end = "/* End generated Thrift service includes */", newContent = newContent)
    }

    val thriftSourceFile = rootDir / "doc" / "src" / "main" / "thrift" / "services.thrift"
    val newContent = thriftFieldsFor(thriftSourceFile.contentAsString)
    thriftSourceFile.overwrite(newContent)

    "services.thrift" -> newContent
  }
}
