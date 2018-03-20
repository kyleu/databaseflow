package services.scalaexport.db.file

import models.scalaexport.db.ExportEnum
import models.scalaexport.file.RestQueryFile

object EnumRestQueryFile {
  def export(enum: ExportEnum) = {
    val key = enum.className + "GetAll"
    val file = RestQueryFile(enum.modelPackage, key)

    file.add(s"/* TODO */")

    file
  }
}
