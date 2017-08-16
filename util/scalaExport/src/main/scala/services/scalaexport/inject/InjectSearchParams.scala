package services.scalaexport.inject

import models.scalaexport.ExportResult
import services.scalaexport.config.ExportModel

object InjectSearchParams {
  def fromString(exportResult: ExportResult, s: String) = InjectSearchParams(exportResult.getModel(s))
}

case class InjectSearchParams(model: ExportModel) {
  val viewClass = (model.viewHtmlPackage :+ (model.propertyName + "SearchResult")).mkString(".")
  val message = model.pkFields match {
    case Nil => s"""s"${model.className} matched [$$q].""""
    case cols => s"""s"${model.className} [${cols.map(x => "${model." + x.propertyName + "}").mkString(", ")}] matched [$$q].""""
  }

  override def toString = s"${model.propertyName}"
}
