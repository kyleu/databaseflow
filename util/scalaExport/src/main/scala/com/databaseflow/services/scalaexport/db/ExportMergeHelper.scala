package com.databaseflow.services.scalaexport.db

import better.files._
import com.databaseflow.models.scalaexport.file.OutputFile
import com.databaseflow.services.scalaexport.ExportFiles

object ExportMergeHelper {
  def writeCore(coreDir: File, log: String => Unit) = {
    val core = ExportFiles.coreLocation.toFile
    core.listRecursively.filter(_.isRegularFile).map { c =>
      val p = c.pathAsString.substring(c.pathAsString.indexOf("coreexport") + 11)
      val tgt = coreDir / p
      if (tgt.exists) {
        val tgtContent = tgt.contentAsString
        if (!tgtContent.contains("Generated File")) {
          log(s"Skipping modified file [${tgt.pathAsString}].")
          "modified"
        } else if (tgtContent == c.contentAsString) {
          "same-content"
        } else {
          c.copyTo(tgt, overwrite = true)
          "overwrite"
        }
      } else {
        tgt.createIfNotExists(createParents = true)
        c.copyTo(tgt, overwrite = true)
        "create"
      }
    }.toSeq
  }

  def writeSource(rootDir: File, log: String => Unit) = {
    val src = ExportFiles.rootLocation.toFile
    src.listRecursively.filter(_.isRegularFile).map { c =>
      val p = c.pathAsString.substring(c.pathAsString.indexOf("scalaexport") + 12)
      val tgt = rootDir / p
      if (tgt.exists) {
        val tgtContent = tgt.contentAsString
        if (!tgtContent.contains("Generated File")) {
          log(s"Skipping modified file [${tgt.pathAsString}].")
          "modified"
        } else if (tgtContent == c.contentAsString) {
          "same-content"
        } else {
          c.copyTo(tgt, overwrite = true)
          "overwrite"
        }
      } else {
        tgt.createIfNotExists(createParents = true)
        c.copyTo(tgt, overwrite = true)
        "create"
      }
    }.toSeq
  }

  def writeFiles(dir: File, files: Seq[OutputFile], log: String => Unit) = files.map { rf =>
    val f = dir / rf.packageDir / rf.filename
    if (f.exists) {
      val tgtContent = f.contentAsString
      if (!tgtContent.contains("Generated File")) {
        log(s"Skipping modified root file [${f.pathAsString}].")
        "modified-root"
      } else if (tgtContent == rf.rendered) {
        // result.log(s"Skipping unchanged root file [${tgt.pathAsString}].")
        "same-content-root"
      } else {
        f.delete(swallowIOExceptions = true)
        f.createIfNotExists()
        f.writeText(rf.rendered)
        "overwrite-root"
      }
    } else {
      f.parent.createDirectories()
      f.writeText(rf.rendered)
      "create-root"
    }
  }
}
