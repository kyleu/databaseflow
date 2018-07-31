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
    pkgPrefix: List[String] = Nil,
    values: Seq[String],
    shared: Boolean = false,
    ignored: Boolean = false
) {
  val valuesWithClassNames = values.map(v => v -> ExportHelper.toClassName(ExportHelper.toIdentifier(v.replaceAllLiterally(".", "_"))))

  val propertyName = ExportHelper.toIdentifier(className)
  val modelPackage = pkgPrefix ++ List("models") ++ pkg
  val tablePackage = pkgPrefix ++ List("models", "table") ++ pkg
  val controllerPackage = pkgPrefix ++ List("controllers", "admin") ++ (if (pkg.isEmpty) { List("system") } else { pkg })
  val controllerClass = (controllerPackage :+ (className + "Controller")).mkString(".")
  val fullClassName = (modelPackage :+ className).mkString(".")
}
