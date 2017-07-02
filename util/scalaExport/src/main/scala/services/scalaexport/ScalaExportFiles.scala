package services.scalaexport

import better.files._
import models.scalaexport.ExportResult

object ScalaExportFiles {
  private[this] val rootDir = "./tmp/testproject".toFile

  def reset() = {
    if (rootDir.exists) {
      rootDir.delete()
    }
    rootDir.createDirectory()
  }

  def persist(result: ExportResult) = {
    result.files.map { file =>
      val f = rootDir / file._1
      f.createIfNotExists(createParents = true)
      f.writeText(file._2)
    }
  }
}
