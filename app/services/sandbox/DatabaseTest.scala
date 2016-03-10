package services.sandbox

import java.sql.ResultSet

import com.zaxxer.hikari.HikariDataSource
import models.database.{ Row, SingleRowQuery }
import org.apache.ddlutils.PlatformFactory
import services.database.{ DatabaseService, MasterDatabase }
import services.schema.SchemaConverter
import utils.ApplicationContext

import upickle.legacy._

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
    while (rs.next()) {
      val row = (1 to columns.getColumnCount).map(i => Option(rs.getObject(i)).map(_.toString).getOrElse("-null-"))
      ret += (whitespace + row.mkString(", "))
    }
    if (showColumns) {
      val cols = (1 to columns.getColumnCount).map(columns.getColumnLabel)
      val colsLabel = whitespace + cols.map(x => "[" + x + "]").mkString(", ")
      colsLabel + "\n" + ret.mkString("\n")
    } else {
      ret.mkString("\n")
    }
  }

  override def run(ctx: ApplicationContext) = {
    import utils.json.JsonSerializers._

    DatabaseService.init()

    val src = MasterDatabase.db.source
    val platform = PlatformFactory.createNewPlatformInstance(src)

    val javaModel = platform.readModelFromDatabase("model")

    val model = SchemaConverter.convert(javaModel)

    val ret = write(model)

    Future.successful(ret)
  }
}
