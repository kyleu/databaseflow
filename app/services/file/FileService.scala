package services.file

import utils.Logging

import scala.util.Random

object FileService extends Logging {
  private[this] var initialized = false

  private[this] val configDir = {
    val osName = System.getProperty("os.name").toUpperCase
    val (homeDir, programFilename) = if (osName.contains("WIN")) {
      new java.io.File(System.getenv("APPDATA")) -> "Database Flow"
    } else {
      new java.io.File(System.getProperty("user.home")) -> ".databaseflow"
    }

    if (homeDir.exists) {
      if (!homeDir.canWrite) {
        throw new IllegalStateException(s"Can't write to config root [$homeDir].")
      }
      val programDir = new java.io.File(homeDir, programFilename)
      if (!programDir.exists) {
        programDir.mkdir()
      }
      if (!programDir.isDirectory) {
        throw new IllegalStateException(s"Non-directory [$programFilename] found in config root [$programDir].")
      }
      if (!programDir.canWrite) {
        throw new IllegalStateException(s"Can't write to [$programFilename] in config root [$programDir].")
      }
      programDir
    } else {
      throw new IllegalStateException(s"Cannot read home directory [$homeDir].")
    }
  }

  def init() = {
    log.info(s"File service initialized, using [${configDir.getAbsolutePath}] as home directory.")
    if (initialized) {
      throw new IllegalStateException("Initialized called more than once for [FileService].")
    }
    initialized = true
  }

  def getConfigFile(filename: String) = new java.io.File(configDir, filename)

  @scala.annotation.tailrec
  def getTempFile(name: String, extension: String): java.io.File = {
    val ret = new java.io.File("./tmp", s"$name.$extension")
    if (!ret.exists()) {
      ret
    } else {
      getTempFile(name + Random.alphanumeric.take(1), extension)
    }
  }
}
