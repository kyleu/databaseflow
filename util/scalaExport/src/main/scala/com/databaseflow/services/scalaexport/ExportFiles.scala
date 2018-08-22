package com.databaseflow.services.scalaexport

import better.files._
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.db.{ExportModel, ExportResult}
import com.databaseflow.models.scalaexport.file.OutputFile
import com.databaseflow.models.scalaexport.thrift.ThriftParseResult
import com.databaseflow.services.scalaexport.db.file._

object ExportFiles {
  var rootLocation = "./tmp/scalaexport"
  var coreLocation = "./tmp/coreexport"

  def prepareRoot(remove: Boolean = true) = {
    val rootDir = rootLocation.toFile
    if (rootDir.exists && remove) {
      rootDir.delete()
    }
    if (!rootDir.exists) {
      rootDir.createDirectories()
    }
    val coreDir = coreLocation.toFile
    if (coreDir.exists && remove) {
      coreDir.delete()
    }
    if (!coreDir.exists) {
      coreDir.createDirectories()
    }
    coreDir -> rootDir
  }

  def persistThrift(result: ThriftParseResult, rootDir: (File, File)) = {
    result.allFiles.map { file =>
      val f = if (file.pkg.isEmpty) {
        rootDir._2 / file.dir / file.filename
      } else {
        rootDir._2 / file.packageDir / file.filename
      }
      f.createIfNotExists(createParents = true)
      f.writeText(file.rendered)
    }
  }

  def persist(result: ExportResult, rootDir: (File, File)) = {
    (result.enumFiles ++ result.sourceFiles).map { file =>
      val d = if (file.core) { rootDir._1 } else { rootDir._2 }
      val f = if (file.pkg.isEmpty) {
        d / file.dir / file.filename
      } else {
        d / file.packageDir / file.filename
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
    Seq(EnumFile.export(e), EnumColumnTypeFile.export(config, e), EnumSchemaFile.export(config, e), EnumControllerFile.export(config, e)) ++ gq ++ oq
  }

  def exportModel(config: ExportConfiguration, model: ExportModel): (ExportModel, Seq[OutputFile]) = {
    if (model.provided) {
      model -> Seq.empty
    } else {
      val cls = ModelFile.export(config, model)
      val res = ResultFile.export(config, model)
      val queries = QueriesFile.export(config, model)
      val table = TableFile.export(config, model)
      val svc = ServiceFile.export(config, model)

      val sch = SchemaFile.export(config, model)
      val cntr = ControllerFile.export(config, model)

      val tm = ThriftModelFile.export(model)
      val ts = ThriftServiceFile.export(model)

      val tdr = TwirlDataRowFile.export(config, model)
      val tl = TwirlListFile.export(config, model)
      val tv = TwirlViewFile.export(config, model)
      val tf = TwirlFormFile.export(config, model)
      val tsr = TwirlSearchResultFile.export(config, model)

      val trs = TwirlRelationFiles.export(config, model)

      val gq = if (config.flags("graphql")) { GraphQLQueryFiles.export(config, model) } else { Nil }
      val solo = config.packages.find(_._2.contains(model)).map(_._4).getOrElse(throw new IllegalStateException(s"Can't find model [$model]."))
      val oq = if (config.flags("openapi")) {
        Seq(OpenApiSchemaFile.export(model, config.enums), OpenApiPathsFile.export(model, config.enums, solo))
      } else {
        Nil
      }

      model -> (Seq(cls, res, queries, table, svc, sch, cntr, tm, ts, tdr, tl, tv, tf, tsr) ++ gq ++ oq ++ trs)
    }
  }
}
