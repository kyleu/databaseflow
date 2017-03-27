package services.database

import java.io.FileOutputStream
import java.util.UUID

import models.connection.ConnectionSettings
import models.engine.DatabaseEngine
import models.queries.query.SavedQueryQueries
import models.query.SavedQuery
import models.user.Permission
import org.apache.commons.io.IOUtils
import services.config.ConfigFileService
import services.connection.ConnectionSettingsService
import services.database.core.MasterDatabase
import utils.{Logging, SlugUtils}

object SampleDatabaseService extends Logging {
  private[this] val databaseName = "sampledb"
  private[this] val filename = databaseName + ".sqlite"
  private[this] val resourcePath = s"$databaseName/$filename"

  def apply(owner: UUID) = {
    val sampleFile = getSampleFile
    val name = "SQLite Music Database"
    val cs = ConnectionSettings(
      id = UUID.randomUUID,
      name = name,
      slug = SlugUtils.slugFor(name),
      owner = owner,
      edit = Permission.Administrator,
      description = "A sample SQLite database containing music information.",
      engine = DatabaseEngine.SQLite,
      dbName = Some(sampleFile.getPath)
    )
    ConnectionSettingsService.insert(cs)
    createSavedQueries(owner, cs.id)
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

  private[this] def createSavedQueries(owner: UUID, connId: UUID) = {
    def q(name: String, sql: String) = SavedQuery(name = name, sql = sql, owner = owner, connection = Some(connId))

    val queries = Seq(
      q("Test Query", "select * from table"),
      q("Test Query 2", "select * from table2")
    )

    queries.foreach { q =>
      MasterDatabase.executeUpdate(SavedQueryQueries.insert(q))
    }
  }
}
