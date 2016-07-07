package services.sandbox

import models.database.{Row, SingleRowQuery}
import models.queries.connection.ConnectionSettingsQueries
import services.database.{MasterDatabase, MasterDatabaseConnection}
import services.schema.SchemaService
import upickle.json
import utils.ApplicationContext

import scala.concurrent.Future
import scala.util.{Failure, Success}

object DatabaseTest extends SandboxTask {
  override def id = "dbtest"
  override def name = "Database Test"
  override def description = ""

  case object TestQuery extends SingleRowQuery[Int] {
    override def sql = "select count(*) as c from users"
    override def map(row: Row) = row.as[Long]("c").toInt
  }

  override def run(ctx: ApplicationContext) = {
    val connections = MasterDatabaseConnection.query(ConnectionSettingsQueries.getAll())

    val connTables = connections.map { c =>
      val db = MasterDatabase.db(c.id)
      val metadata = SchemaService.getSchema(db)
      c -> metadata
    }
    val ret = connTables.map { t =>
      import upickle.default._

      val tableStrings = t._2 match {
        case Success(s) => s.tables.map { x =>
          json.write(writeJs(x), 2)
        }
        case Failure(x) => throw x
      }
      s"${t._1.id}\n${t._1.name}\n" + tableStrings.mkString("\n") + "\n\n"
    }
    Future.successful(ret.mkString("\n"))
  }
}
