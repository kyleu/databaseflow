package services.scalaexport

import better.files._
import models.scalaexport.{ExportResult, OutputFile}
import models.schema.Schema
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

  def exportTable(s: Schema, et: ExportTable): (ExportTable, Seq[OutputFile]) = {
    if (et.config.provided.contains(et.propertyName)) {
      et -> Seq.empty
    } else {
      val cls = ModelFile.export(et)
      val res = ResultFile.export(et)
      val queries = QueriesFile.export(et)
      val svc = ServiceFile.export(et)
      val sch = SchemaFile.export(et)
      val cntr = ControllerFile.export(et)

      val tl = TwirlListFile.export(et)
      val tv = TwirlViewFile.export(et)
      val tf = TwirlFormFile.export(et)
      val tsr = TwirlSearchResultFile.export(et)

      et -> Seq(cls, res, queries, svc, sch, cntr, tl, tv, tf, tsr)
    }
  }
}
