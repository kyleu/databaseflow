package services.scalaexport

import better.files._
import models.scalaexport.ExportResult

object ExportFiles {
  def reset(projectName: String) = {
    val rootDir = s"./tmp/$projectName".toFile
    if (rootDir.exists) {
      rootDir.delete()
    }
    rootDir.createDirectory()
  }

  def persist(result: ExportResult) = {
    val rootDir = s"./tmp/${result.id}".toFile
    result.files.map { file =>
      val f = rootDir / file._1
      f.createIfNotExists(createParents = true)
      f.writeText(file._2)
    }
  }
}
