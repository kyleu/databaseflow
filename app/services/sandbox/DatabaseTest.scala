package services.sandbox

import java.sql.ResultSet

import models.database.{Row, SingleRowQuery}
import services.database.{MasterDatabase, DatabaseService}
import utils.{NullUtils, ApplicationContext}

import scala.concurrent.Future

object DatabaseTest extends SandboxTask {
  override def id = "dbtest"
  override def name = "Database Test"

  override def description = ""

  case object TestQuery extends SingleRowQuery[Int] {
    override def sql = "select count(*) as c from users"
    override def map(row: Row) = row.as[Long]("c").toInt
  }

  def rsToString(rs: ResultSet, indent: Int = 0, showColumns: Boolean = false) = {
    val whitespace = (0 until indent).map(x => " ").mkString
    val ret = collection.mutable.ArrayBuffer.empty[String]
    val columns = rs.getMetaData
    while(rs.next()) {
      val row = (1 to columns.getColumnCount).map(i => Option(rs.getObject(i)).map(_.toString).getOrElse("-null-"))
      ret += (whitespace + row.mkString(", "))
    }
    if(showColumns) {
      val cols = (1 to columns.getColumnCount).map(columns.getColumnLabel)
      val colsLabel = whitespace + cols.map(x => "[" + x + "]").mkString(", ")
      colsLabel + "\n" + ret.mkString("\n")
    } else {
      ret.mkString("\n")
    }
  }

  override def run(ctx: ApplicationContext) = {
    DatabaseService.init()

    val conn = MasterDatabase.db.source.getConnection()

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
      ret += rsToString(md.getSchemas, 2, showColumns = true)
      ret += "Tables"
      ret += rsToString(md.getTables(NullUtils.inst, NullUtils.inst, NullUtils.inst, NullUtils.inst), 2, showColumns = true)
      ret += ""
      ret += "   "
      ret += ""
      ret += "   "
    } finally {
      conn.close()
    }

    Future.successful(ret.mkString("\n"))
  }
}
