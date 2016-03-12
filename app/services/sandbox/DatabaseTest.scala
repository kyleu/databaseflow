package services.sandbox

import models.database.{ Row, SingleRowQuery }
import models.queries.connection.ConnectionQueries
import models.templates.SchemaTemplate
import services.database.MasterDatabase
import services.schema.MetadataService
import upickle.json
import utils.ApplicationContext

import scala.concurrent.Future

object DatabaseTest extends SandboxTask {
  override def id = "dbtest"
  override def name = "Database Test"
  override def description = ""

  case object TestQuery extends SingleRowQuery[Int] {
    override def sql = "select count(*) as c from users"
    override def map(row: Row) = row.as[Long]("c").toInt
  }

  override def run(ctx: ApplicationContext) = {
    val connections = MasterDatabase.db.query(ConnectionQueries.getAll())

    val connTables = connections.map { c =>
      val db = MasterDatabase.databaseFor(c.id)
      val metadata = MetadataService.getMetadata(db.source)
      c -> metadata
    }
    val ret = connTables.map { t =>
      import utils.JsonSerializers._
      import upickle.legacy._

      val html = SchemaTemplate.forTables(t._2)

      val tableStrings = t._2.map { x =>
        val j = writeJs(x)
        json.write(j, 2)
      }
      html.toString + s"\n\n${t._1.id}\n${t._1.name}\n" + tableStrings.mkString("\n") + "\n\n"
    }
    Future.successful(ret.mkString("\n"))
  }
}
