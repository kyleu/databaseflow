package services.database

import models.engine.DatabaseEngine
import models.engine.rdbms.{ H2, MySQL, PostgreSQL }

import scala.io.Source

object SampleDatabaseService {
  def apply(connection: DatabaseConnection) = {
    val source = getSampleFile(connection.engine)

  }

  private[this] def getSampleFile(engine: DatabaseEngine) = {
    val filename = engine match {
      case MySQL => "Chinook_MySql.sql"
      case PostgreSQL => "Chinook_PostgreSql.sql"
      case H2 => "Chinook_PostgreSql.sql"
      case x => throw new IllegalStateException(s"No sample database avilable for [${x.getClass.getSimpleName}].")
    }
    Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream("sampledb/" + filename))
  }
}
