package models.scalaexport.db

import services.scalaexport.ExportHelper

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
