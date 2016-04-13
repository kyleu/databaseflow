package services.database

import java.util.UUID

import akka.actor.ActorRef
import models.{ BatchQueryStatus, TableResultResponse }
import models.database.Statement
import models.engine.DatabaseEngine
import models.engine.rdbms._
import services.schema.SchemaService
import utils.{ DateUtils, Logging }

import scala.io.Source
import scala.util.{ Failure, Success }

object SampleDatabaseService extends Logging {
  def apply(connection: DatabaseConnection, queryId: UUID, statusActor: ActorRef) = {
    log.info(s"Creating sample database for ${connection.name}")
    val source = getSampleFile(connection.engine)
    val content = source.getLines.toSeq.mkString("\n")
    val statements = content.split(";").map(_.trim).filter(_.nonEmpty)

    val startMs = DateUtils.nowMillis
    var completedQueries = 0
    connection.transaction { t =>
      statements.foreach { statement =>
        t.execute(new Statement {
          override def sql = statement
        })
        completedQueries += 1
        if (completedQueries < 10 || (completedQueries < 100 && completedQueries % 10 == 0) || (completedQueries % 100 == 0)) {
          statusActor ! BatchQueryStatus(queryId, completedQueries, statements.length - completedQueries, (DateUtils.nowMillis - startMs).toInt)
        }

      }
      statusActor ! BatchQueryStatus(queryId, completedQueries, statements.length - completedQueries, (DateUtils.nowMillis - startMs).toInt)
    }

    SchemaService.getSchema(connection, forceRefresh = true) match {
      case Success(s) => statusActor ! TableResultResponse(s.tables, 0)
      case Failure(x) => throw x
    }

  }

  private[this] def getSampleFile(engine: DatabaseEngine) = {
    val filename = engine match {
      case H2 => "Chinook_MySql.sql"
      case MySQL => "Chinook_MySql.sql"
      case Oracle => "Chinook_Oracle.sql"
      case PostgreSQL => "Chinook_PostgreSql.sql"
      case SqlServer => "Chinook_SqlServer.sql"
      case x => throw new IllegalStateException(s"No sample database avilable for [${x.getClass.getSimpleName}].")
    }
    Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream("sampledb/" + filename))
  }
}
