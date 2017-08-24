package services.scalaexport

import better.files._
import models.scalaexport.{ExportResult, OutputFile}
import services.scalaexport.config.{ExportConfiguration, ExportModel}
import services.scalaexport.file._

object ExportFiles {
  def persist(result: ExportResult) = {
    val rootDir = "./tmp/scalaexport".toFile
    if (rootDir.exists) { rootDir.delete() }
    rootDir.createDirectory()

    result.sourceFiles.map { file =>
      val f = if (file.pkg.isEmpty) {
        rootDir / file.dir / file.filename
      } else {
        rootDir / file.packageDir / file.filename
      }
      f.createIfNotExists(createParents = true)
      f.writeText(file.rendered)
    }

    result.log("File write complete.")
  }

  def exportModel(config: ExportConfiguration, model: ExportModel): (ExportModel, Seq[OutputFile]) = {
    if (model.provided) {
      model -> Seq.empty
    } else {
      val cls = ModelFile.export(model)
      val res = ResultFile.export(model)
      val queries = QueriesFile.export(config.engine, model)
      val svc = ServiceFile.export(model)
      val sch = SchemaFile.export(config, model)
      val cntr = ControllerFile.export(model)

      val tl = TwirlListFile.export(model)
      val tr = TwirlRelationsFile.export(model)
      val tv = TwirlViewFile.export(model)
      val tf = TwirlFormFile.export(model)

      val tsr = TwirlSearchResultFile.export(model)

      model -> Seq(cls, res, queries, svc, sch, cntr, tl, tr, tv, tf, tsr)
    }
  }
}
