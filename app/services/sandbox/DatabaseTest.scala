package services.sandbox

import java.sql.ResultSet

import models.database.{Row, SingleRowQuery}
import services.database.DatabaseService
import services.history.HistoryDatabase
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

  def rsToString(rs: ResultSet, indent: Int = 0) = {
    val whitespace = (0 until indent).map(x => " ").mkString
    val ret = collection.mutable.ArrayBuffer.empty[String]
    val columns = rs.getMetaData
    while(rs.next()) {
      val row = (1 to columns.getColumnCount).map(i => rs.getObject(i).toString)
      ret += (whitespace + row.mkString(", "))
    }
    ret.mkString("\n")
  }

  override def run(ctx: ApplicationContext) = {
    DatabaseService.init()

    val conn = HistoryDatabase.db.source.getConnection()

    var ret = collection.mutable.ArrayBuffer.empty[String]

    try {
      val md = conn.getMetaData

      ret += "Database:"
      ret += "  " + md.getDatabaseProductName + " " + md.getDatabaseProductVersion
      ret += "Catalog Name:"
      ret += "  " + md.getCatalogTerm
      ret += "Catalogs:"
      ret += rsToString(md.getCatalogs, 2)
      ret += "Schemas:"
      ret += rsToString(md.getSchemas, 2)
      ret += ""
      ret += "   "
      ret += ""
      ret += "   "
      ret += ""
      ret += "   "
    } finally {
      conn.close()
    }

    HistoryDatabase.close()
    Future.successful(ret.mkString("\n"))
  }
}
