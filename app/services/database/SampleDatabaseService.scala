package services.database

import java.io.FileOutputStream
import java.util.UUID

import models.connection.ConnectionSettings
import models.engine.DatabaseEngine
import models.user.Permission
import org.apache.commons.io.IOUtils
import services.config.ConfigFileService
import services.connection.ConnectionSettingsService
import utils.Logging

object SampleDatabaseService extends Logging {
  private[this] val databaseName = "sampledb"
  private[this] val filename = databaseName + ".sqlite"
  private[this] val resourcePath = s"$databaseName/$filename"

  def apply(owner: UUID) = {
    val sampleFile = getSampleFile

    val cs = ConnectionSettings(
      id = UUID.randomUUID,
      name = "SQLite Music Database",
      owner = owner,
      edit = Permission.User,
      description = "A sample SQLite database containing music information.",
      engine = DatabaseEngine.SQLite,
      dbName = Some(sampleFile.getPath)
    )

    ConnectionSettingsService.insert(cs)

    cs
  }

  private[this] def getSampleFile = {
    val f = new java.io.File(ConfigFileService.configDir, filename)
    if (!f.exists()) {
      writeSampleFile(f)
    }
    f
  }

  private[this] def writeSampleFile(f: java.io.File) = {
    val is = getClass.getClassLoader.getResourceAsStream(resourcePath)
    val os = new FileOutputStream(f)
    IOUtils.copy(is, os)
    os.close()
  }
}
