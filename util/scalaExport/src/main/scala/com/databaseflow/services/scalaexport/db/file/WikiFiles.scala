package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.{ExportEnum, ExportModel}
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration

object WikiFiles {
  def export(config: ExportConfiguration, models: Seq[ExportModel]) = {
    exportIndex(config, models) ++ models.flatMap(exportModel(config, _)) ++ config.enums.flatMap(exportEnum(config, _))
  }

  def exportModel(config: ExportConfiguration, model: ExportModel) = config.wikiLocation match {
    case Some(l) => Seq(WikiTableFile.export(config, model))
    case None => Nil
  }

  def exportEnum(config: ExportConfiguration, enum: ExportEnum) = config.wikiLocation match {
    case Some(l) => Seq(WikiEnumFile.export(config, enum))
    case None => Nil
  }

  def exportIndex(config: ExportConfiguration, models: Seq[ExportModel]) = config.wikiLocation match {
    case Some(l) => WikiListFiles.export(config, models)
    case None => Nil
  }
}
