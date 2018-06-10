package services.scalaexport

import better.files._
import models.scalaexport.db.config.ExportConfiguration
import models.scalaexport.db.{ExportModel, ExportResult}
import models.scalaexport.file.OutputFile
import models.scalaexport.thrift.ThriftParseResult
import services.scalaexport.db.file._

object ExportFiles {
  var rootLocation = "./tmp/scalaexport"

  def prepareRoot(remove: Boolean = true) = {
    val rootDir = rootLocation.toFile
    if (rootDir.exists && remove) {
      rootDir.delete()
    }
    if (!rootDir.exists) {
      rootDir.createDirectory()
    }
    rootDir
  }

  def persistThrift(result: ThriftParseResult, rootDir: File) = {
    result.allFiles.map { file =>
      val f = if (file.pkg.isEmpty) {
        rootDir / file.dir / file.filename
      } else {
        rootDir / file.packageDir / file.filename
      }
      f.createIfNotExists(createParents = true)
      f.writeText(file.rendered)
    }
  }

  def persist(result: ExportResult, rootDir: File) = {
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

  def exportEnums(config: ExportConfiguration) = config.enums.flatMap { e =>
    if (config.models.exists(_.className == e.className)) {
      throw new IllegalStateException(s"Please rename the class of enum [${e.name}], the class name is already in use.")
    }
    val gq = if (config.flags("graphql")) { Seq(EnumGraphQLQueryFile.export(e)) } else { Nil }
    val oq = if (config.flags("openapi")) { Seq(EnumOpenApiSchemaFile.export(e), EnumOpenApiPathsFile.export(e)) } else { Nil }
    val rp = config.rootPrefix
    Seq(EnumFile.export(e), EnumColumnTypeFile.export(rp, e), EnumSchemaFile.export(rp, e), EnumControllerFile.export(rp, e)) ++ gq ++ oq
  }

  def exportModel(config: ExportConfiguration, model: ExportModel): (ExportModel, Seq[OutputFile]) = {
    if (model.provided) {
      model -> Seq.empty
    } else {
      val cls = ModelFile.export(config, model, config.modelLocationOverride)
      val res = ResultFile.export(config.rootPrefix, model, config.modelLocationOverride)
      val queries = QueriesFile.export(config.rootPrefix, model)
      val svc = ServiceFile.export(config.rootPrefix, model)
      val cntr = ControllerFile.export(config, model)
      val sch = SchemaFile.export(config, model)
      val table = TableFile.export(config.rootPrefix, model, config.enums)

      val tm = ThriftModelFile.export(model, config.enums)
      val ts = ThriftServiceFile.export(model, config.enums)

      val tdr = TwirlDataRowFile.export(config, model)
      val tl = TwirlListFile.export(config.rootPrefix, model)
      val tv = TwirlViewFile.export(config, model)
      val tf = TwirlFormFile.export(config, model)
      val tsr = TwirlSearchResultFile.export(config.rootPrefix, model)

      val trs = TwirlRelationFiles.export(config, model)

      val gq = if (config.flags("graphql")) { GraphQLQueryFiles.export(config, model) } else { Nil }
      val solo = config.packages.find(_._2.contains(model)).map(_._4).getOrElse(throw new IllegalStateException(s"Can't find model [$model]."))
      val oq = if (config.flags("openapi")) {
        Seq(OpenApiSchemaFile.export(model, config.enums), OpenApiPathsFile.export(model, config.enums, solo))
      } else {
        Nil
      }

      model -> (Seq(cls, res, queries, svc, cntr, sch, table, tm, ts, tdr, tl, tv, tf, tsr) ++ gq ++ oq ++ trs)
    }
  }
}
