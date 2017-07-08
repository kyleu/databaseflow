package services.scalaexport

import better.files._
import models.scalaexport.ExportResult
import models.schema.{Schema, Table}
import services.scalaexport.file.{ClassFile, QueriesFile, SchemaFile, ServiceFile}

object ExportFiles {
  def persist(result: ExportResult) = {
    val rootDir = "./tmp/scalaexport".toFile
    if (rootDir.exists) { rootDir.delete() }
    rootDir.createDirectory()

    result.files.map { file =>
      val f = if (file.pkg.isEmpty) {
        rootDir / file.filename
      } else {
        rootDir / file.pkg.mkString("/") / file.filename
      }
      f.createIfNotExists(createParents = true)
      f.writeText(file.rendered)
    }
  }

  def exportTable(s: Schema, et: ExportTable) = {
    val cls = ClassFile.export(et)
    val queries = QueriesFile.export(et)
    val svc = ServiceFile.export(et)
    val sch = SchemaFile.export(et)
    et -> Seq(cls, queries, svc, sch)
  }
}
