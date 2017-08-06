package services.scalaexport

import better.files._
import models.scalaexport.ExportResult
import models.schema.Schema
import services.scalaexport.config.ExportConfig
import services.scalaexport.file._

object ExportFiles {
  def persist(result: ExportResult) = {
    val rootDir = "./tmp/scalaexport".toFile
    if (rootDir.exists) { rootDir.delete() }
    rootDir.createDirectory()

    result.files.map { file =>
      val f = if (file.pkg.isEmpty) {
        rootDir / file.filename
      } else {
        rootDir / file.pkg.mkString("/") / file.filename
      }
      f.createIfNotExists(createParents = true)
      f.writeText(file.rendered)
    }

    result.log("File write complete.")
  }

  def exportTable(s: Schema, et: ExportTable, result: ExportConfig.Result) = {
    val cls = ModelFile.export(et)
    val queries = QueriesFile.export(et)
    val svc = ServiceFile.export(et)
    val sch = SchemaFile.export(et, result)
    val cntr = ControllerFile.export(et)

    val tl = TwirlListFile.export(et)
    val tv = TwirlViewFile.export(et)
    val tsr = TwirlSearchResultFile.export(et)

    et -> Seq(cls, queries, svc, sch, cntr, tl, tv, tsr)
  }
}
