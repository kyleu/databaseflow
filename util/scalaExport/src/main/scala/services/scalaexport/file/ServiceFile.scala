package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.Table

object ServiceFile {
  def export(className: String, pkg: Seq[String], table: Table) = {
    val file = ScalaFile("services" +: pkg, className + "Service")
    file.addImport(("models" +: pkg).mkString("."), className)
    file.add(s"object ${className}Service {", 1)
    file.add("}", -1)
    ("services" +: pkg, file.filename, file.render())
  }
}
