package com.databaseflow.services.scalaexport.db.inject

import com.databaseflow.models.scalaexport.db.{ExportModel, ExportResult}

object InjectSearchParams {
  def fromString(exportResult: ExportResult, s: String) = InjectSearchParams(exportResult.getModel(s))
}

case class InjectSearchParams(model: ExportModel) {
  val viewClass = (model.viewHtmlPackage :+ (model.propertyName + "SearchResult")).mkString(".")
  val message = model.pkFields match {
    case Nil => s"""s"${model.title} matched [$$q].""""
    case cols => s"""s"${model.title} [${cols.map(x => "${model." + x.propertyName + "}").mkString(", ")}] matched [$$q].""""
  }

  override def toString = s"${model.propertyName}"
}
