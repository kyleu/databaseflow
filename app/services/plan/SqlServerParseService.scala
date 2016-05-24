package services.plan

import java.sql.PreparedStatement
import java.util.UUID

import models.database.{ Query, Row, Statement }
import models.plan.{ PlanError, PlanNode, PlanResult }

import scala.util.control.NonFatal

object SqlServerParseService extends PlanParseService("sqlserver") {
  val enableExplain = new Statement { override def sql = "SET SHOWPLAN_XML ON" }
  val disableExplain = new Statement { override def sql = "SET SHOWPLAN_XML OFF" }
  val enableAnalyze = new Statement { override def sql = "SET STATISTICS XML ON" }
  val disableAnalyze = new Statement { override def sql = "SET STATISTICS XML OFF" }

  case class SqlServerExplainQuery(override val sql: String) extends Query[String] {
    override def reduce(stmt: PreparedStatement, rows: Iterator[Row]) = {
      "?"
    }
  }

  override def parse(sql: String, queryId: UUID, plan: String, startMs: Long) = try {
    Right(PlanResult(
      queryId = queryId,
      action = "Action",
      sql = sql,
      raw = plan,
      node = PlanNode("title", "nodeType"),
      occurred = startMs
    ))
  } catch {
    case NonFatal(x) => Left(PlanError(
      queryId = queryId,
      sql = sql,
      code = x.getClass.getSimpleName,
      message = x.getMessage,
      raw = Some(plan),
      occurred = startMs
    ))
  }
}
