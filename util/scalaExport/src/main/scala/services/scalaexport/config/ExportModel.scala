package services.scalaexport.config

import models.schema.{Column, ForeignKey}
import services.scalaexport.ExportHelper

object ExportModel {
  case class Reference(name: String, srcTable: String, srcCol: String, tgt: String, notNull: Boolean) {
    val propertyName = ExportHelper.toIdentifier(name)
  }
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
  val fullClassName = (pkg :+ className).mkString(".")
  val pkFields = pkColumns.map(c => getField(c.name))

  val modelPackage = "models" +: pkg
  val queriesPackage = "models" +: "queries" +: pkg
  val servicesPackage = "services" +: pkg
  val controllerPackage = "controllers" +: "admin" +: (if (pkg.isEmpty) { List("system") } else { pkg })
  val routesPackage = controllerPackage :+ "routes"
  val viewPackage = "views" +: "admin" +: pkg
  val viewHtmlPackage = "views" +: "html" +: "admin" +: pkg

  val modelClass = (modelPackage :+ className).mkString(".")
  val serviceClass = (servicesPackage :+ (className + "Service")).mkString(".")
  val controllerClass = (controllerPackage :+ (className + "Controller")).mkString(".")
  val routesClass = (routesPackage :+ (className + "Controller")).mkString(".")

  def getField(k: String) = getFieldOpt(k).getOrElse(throw new IllegalStateException(s"No field for model [$className] with name [$k]."))
  def getFieldOpt(k: String) = fields.find(f => f.columnName == k || f.propertyName == k)
}
