package services.config

import better.files._

import com.typesafe.config.ConfigFactory
import util.Logging

import scala.io.Source

object ConfigFileService extends Logging {
  private[this] var initialized = false

  def init() = {
    log.info(s"File service initialized, using [${configDir.path.toAbsolutePath}] as home directory.")
    if (initialized) {
      throw new IllegalStateException("Initialized called more than once for [FileService].")
    }
    initialized = true
  }

  val configDir = {
    val osName = System.getProperty("os.name").toUpperCase
    val (homeDir, programFilename) = if (osName.contains("WIN")) {
      System.getenv("APPDATA").toFile -> "DatabaseFlow"
    } else {
      val dir = System.getProperty("user.home")
      val cleanDir = if (dir == "/usr/sbin") {
        "/opt/databaseflow".toFile // Docker bullshit
      } else {
        val d = dir.toFile
        if (d.isDirectory && d.isWriteable) {
          d
        } else {
          val share = "/usr/share/" / util.Config.projectId
          if (share.isDirectory && share.isWriteable) {
            share
          } else {
            throw new IllegalStateException("Cannot find directory to write config file.")
          }
        }
      }
      cleanDir -> ".databaseflow"
    }

    if (homeDir.exists) {
      if (!homeDir.isWriteable) { throw new IllegalStateException(s"Can't write to config root [$homeDir].") }
      val programDir = homeDir / programFilename
      if (!programDir.exists) { programDir.createDirectory }
      if (!programDir.isDirectory) { throw new IllegalStateException(s"Non-directory [$programFilename] found in config root [$programDir].") }
      if (!programDir.isWriteable) { throw new IllegalStateException(s"Can't write to [$programFilename] in config root [$programDir].") }
      programDir
    } else {
      throw new IllegalStateException(s"Cannot read home directory [$homeDir].")
    }
  }

  val isDocker = configDir.path.toAbsolutePath.toString.contains("/opt/databaseflow")

  val config = {
    val cfg = configDir / "databaseflow.conf"
    if (!cfg.exists()) {
      val refCnfStream = getClass.getClassLoader.getResourceAsStream("initial-config.conf")
      val refCnf = Source.fromInputStream(refCnfStream).getLines.toSeq.mkString("\n")
      cfg.overwrite(refCnf)
    }
    ConfigFactory.parseFile(cfg.toJava)
  }
}
