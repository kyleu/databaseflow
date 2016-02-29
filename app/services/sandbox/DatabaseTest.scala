package services.sandbox

import models.database.{SingleRowQuery, Row, Query, ConnectionSettings}
import services.database.DatabaseService
import utils.ApplicationContext

import scala.concurrent.Future

object DatabaseTest extends SandboxTask {
  override def id = "dbtest"
  override def name = "Database Test"

  override def description = ""

  case object TestQuery extends SingleRowQuery[Int] {
    override def sql = "select count(*) as c from users"
    override def map(row: Row) = row.getObject("c").toString.toInt
  }

  override def run(ctx: ApplicationContext) = {
    DatabaseService.init()

    val cs = ConnectionSettings(
      url = "jdbc:postgresql://localhost:5432/puzzlebrawl",
      username = "databaseflow",
      password = "flow"
    )
    val db = DatabaseService.connect(cs)

    val ret = db.query(TestQuery)

    Future.successful(ret.toString)
  }
}
