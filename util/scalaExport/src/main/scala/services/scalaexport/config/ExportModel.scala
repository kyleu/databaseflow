package services.scalaexport.config

import models.schema.{Column, ForeignKey}

object ExportModel {
  case class Reference(name: String, srcTable: String, srcCol: String, tgt: String, notNull: Boolean)
}

case class ExportModel(
    tableName: String,
    pkg: List[String] = Nil,
    propertyName: String,
    className: String,
    title: String,
    description: Option[String],
    plural: String,
    fields: List[ExportField],
    pkColumns: List[Column],
    foreignKeys: List[ForeignKey],
    references: List[ExportModel.Reference],
    extendsClass: Option[String] = None,
    ignored: Boolean = false,
    provided: Boolean = false
) {
  def getField(k: String) = getFieldOpt(k).getOrElse(throw new IllegalStateException(s"No field for model [$className] with name [$k]."))
  def getFieldOpt(k: String) = fields.find(f => f.columnName == k || f.propertyName == k)
}
