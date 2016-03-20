package test

import models.engine.rdbms.PostgreSQL
import models.plan.{ PlanNode, PlanResult }
import org.scalatest.{ FlatSpec, Matchers }
import services.plan.PlanParseService

import scala.io.Source

class PlanParseTest extends FlatSpec with Matchers {
  def load(key: String) = {
    val sql = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream(s"$key.sql"))
    val plan = Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream(s"$key.json"))

    val sqlContents = sql.getLines.toSeq.mkString("\n")
    val planContents = plan.getLines.toSeq.mkString("\n")

    sqlContents -> planContents
  }

  "Plan Parser" should "construct without error" in {
    val (sql, plan) = load("mysql-complicated-query")
    val testVal = PlanParseService.parse("", "")(PostgreSQL)
    1 should be(1)
  }

  it should "throw IllegalArgumentException if invalid JSON is passed" in {
    a[IllegalArgumentException] should be thrownBy {
      throw new IllegalArgumentException("!")
    }
  }
}
