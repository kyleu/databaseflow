package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.{Column, Table}

object ServiceFile {
  def export(className: String, table: Table) = {
    val file = ScalaFile("services", className + "Service")
    file.add(s"object ${className}Service {", 1)
    file.add("}", -1)
    file.filename -> file.render()
  }
}
