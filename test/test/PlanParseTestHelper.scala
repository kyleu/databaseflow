package test

import java.util.UUID

import models.engine.DatabaseEngine
import services.plan.PlanParseService

import scala.io.Source

object PlanParseTestHelper {
  private[this] def load(key: String) = {
    val sql = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream(s"$key.sql"))
    val plan = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream(s"$key.json"))

    val sqlContents = sql.getLines.toSeq.mkString("\n")
    val planContents = plan.getLines.toSeq.mkString("\n")

    sqlContents -> planContents
  }

  def test(key: String, engine: DatabaseEngine) = {
    val (sql, plan) = load(key)
    val queryId = UUID.randomUUID
    PlanParseService.parse(sql, queryId, plan, utils.DateUtils.nowMillis)(engine) match {
      case Right(result) => result
      case Left(err) => throw new IllegalStateException(err.toString)
    }
  }
}
