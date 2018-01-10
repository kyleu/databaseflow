package services.scalaexport

import better.files._
import models.scalaexport.{ExportResult, OutputFile}
import services.scalaexport.config.{ExportConfiguration, ExportEnum, ExportModel}
import services.scalaexport.file._

object ExportFiles {
  def persist(result: ExportResult) = {
    val rootDir = "./tmp/scalaexport".toFile
    if (rootDir.exists) { rootDir.delete() }
    rootDir.createDirectory()

    result.enumFiles.map { file =>
      val f = if (file.pkg.isEmpty) {
        rootDir / file.dir / file.filename
      } else {
        rootDir / file.packageDir / file.filename
      }
      f.createIfNotExists(createParents = true)
      f.writeText(file.rendered)
    }

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

  def exportEnums(enums: Seq[ExportEnum]) = enums.map { e =>
    EnumFile.export(e)
  }

  def exportModel(config: ExportConfiguration, model: ExportModel): (ExportModel, Seq[OutputFile]) = {
    if (model.provided) {
      model -> Seq.empty
    } else {
      val cls = ModelFile.export(model)
      val res = ResultFile.export(model)
      val queries = QueriesFile.export(model)
      val table = TableFile.export(model)
      val svc = ServiceFile.export(model)
      val sch = SchemaFile.export(config, model)
      val cntr = ControllerFile.export(config, model)

      val tdr = TwirlDataRowFile.export(config, model)
      val tl = TwirlListFile.export(model)
      val tv = TwirlViewFile.export(config, model)
      val tf = TwirlFormFile.export(config, model)
      val tsr = TwirlSearchResultFile.export(model)

      val trs = TwirlRelationFiles.export(config, model)

      model -> (Seq(cls, res, queries, table, svc, sch, cntr, tdr, tl, tv, tf, tsr) ++ trs)
    }
  }
}
