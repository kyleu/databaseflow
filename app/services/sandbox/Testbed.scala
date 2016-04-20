package services.sandbox

import models.database.{ Row, SingleRowQuery }
import models.queries.connection.ConnectionSettingsQueries
import services.database.MasterDatabase
import services.schema.SchemaService
import upickle.json
import utils.ApplicationContext

import scala.concurrent.Future
import scala.util.{ Failure, Success }

object Testbed extends SandboxTask {
  override def id = "testbed"
  override def name = "Testbed"
  override def description = ""

  override def run(ctx: ApplicationContext) = {
    val connections = MasterDatabase.conn.query(ConnectionSettingsQueries.getAll())

    val connection = connections.headOption.getOrElse(throw new IllegalStateException())
    val db = MasterDatabase.databaseFor(connection.id) match {
      case Right(d) => d
      case Left(x) => throw x
    }
    val schema = SchemaService.getSchema(db)

    val tableStrings = schema match {
      case Success(s) => s.tables.map(_.name)
      case Failure(x) => throw x
    }
    val ret = tableStrings.sorted.mkString("\n")
    Future.successful(ret)
  }
}
