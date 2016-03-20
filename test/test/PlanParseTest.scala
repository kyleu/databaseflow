package test

import models.engine.DatabaseEngine
import models.engine.rdbms.{ MySQL, PostgreSQL }
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

  def test(key: String, engine: DatabaseEngine) = {
    val (sql, plan) = load(key)
    val testVal = PlanParseService.parse(sql, plan)(engine)
    1 should be(1)
  }

  "Plan Parser" should "load basic PostgreSQL plan" in {
    test("postgres-nested-join", PostgreSQL)
  }

  it should "load basic MySQL plan" in {
    test("mysql-nested-loop", MySQL)
  }

  it should "load complex MySQL plan" in {
    test("mysql-complicated-query", MySQL)
  }

  it should "throw IllegalArgumentException if invalid JSON is passed" in {
    a[IllegalArgumentException] should be thrownBy {
      throw new IllegalArgumentException("!")
    }
  }
}
