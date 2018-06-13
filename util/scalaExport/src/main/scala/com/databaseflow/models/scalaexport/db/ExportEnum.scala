package com.databaseflow.models.scalaexport.db

import com.databaseflow.services.scalaexport.ExportHelper
import util.JsonSerializers._

object ExportEnum {
  implicit val jsonEncoder: Encoder[ExportEnum] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportEnum] = deriveDecoder
}

case class ExportEnum(
    pkg: List[String] = Nil,
    name: String,
    className: String,
    values: Seq[String],
    ignored: Boolean = false
) {
  val propertyName = ExportHelper.toIdentifier(className)
  val modelPackage = "models" +: pkg
  val tablePackage = "models" +: "table" +: pkg
  val controllerPackage = "controllers" +: "admin" +: (if (pkg.isEmpty) { List("system") } else { pkg })
  val controllerClass = (controllerPackage :+ (className + "Controller")).mkString(".")
  val fullClassName = (modelPackage :+ className).mkString(".")
}
