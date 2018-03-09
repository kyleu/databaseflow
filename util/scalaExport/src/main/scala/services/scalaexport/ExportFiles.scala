package services.scalaexport

import better.files._
import models.scalaexport.{ExportResult, OutputFile}
import services.scalaexport.config.{ExportConfiguration, ExportModel}
import services.scalaexport.file._
import services.scalaexport.thrift.ThriftParseService

object ExportFiles {
  def persistThrift(result: ThriftParseService.Result) = {
    result.files.map { file =>
      val f = if (file.pkg.isEmpty) {
        rootDir / file.dir / file.filename
      } else {
        rootDir / file.packageDir / file.filename
      }
      f.createIfNotExists(createParents = true)
      f.writeText(file.rendered)
    }
  }

  def persist(result: ExportResult) = {
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

  private[this] lazy val rootDir = {
    val rootDir = "./tmp/scalaexport".toFile
    if (rootDir.exists) { rootDir.delete() }
    rootDir.createDirectory()
    rootDir
  }

  def exportEnums(config: ExportConfiguration) = config.enums.flatMap { e =>
    if (config.models.exists(_.className == e.className)) {
      throw new IllegalStateException(s"Please rename the class of enum [${e.name}], the class name is already in use.")
    }
    Seq(EnumFile.export(e), EnumColumnTypeFile.export(e), EnumSchemaFile.export(e))
  }

  def exportModel(config: ExportConfiguration, model: ExportModel): (ExportModel, Seq[OutputFile]) = {
    if (model.provided) {
      model -> Seq.empty
    } else {
      val cls = ModelFile.export(model)
      val res = ResultFile.export(model)
      val queries = QueriesFile.export(model)
      val svc = ServiceFile.export(model)
      val cntr = ControllerFile.export(config, model)
      val sch = SchemaFile.export(config, model)
      val table = TableFile.export(model, config.enums)

      val tm = ThriftModelFile.export(model, config.enums)
      val ts = ThriftServiceFile.export(model, config.enums)

      val tdr = TwirlDataRowFile.export(config, model)
      val tl = TwirlListFile.export(model)
      val tv = TwirlViewFile.export(config, model)
      val tf = TwirlFormFile.export(config, model)
      val tsr = TwirlSearchResultFile.export(model)

      val trs = TwirlRelationFiles.export(config, model)

      model -> (Seq(cls, res, queries, svc, cntr, sch, table, tm, ts, tdr, tl, tv, tf, tsr) ++ trs)
    }
  }
}
