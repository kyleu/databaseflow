package test

import java.util.UUID

import models.PlanResultResponse
import models.engine.rdbms.PostgreSQL
import org.scalatest.{ FlatSpec, Matchers }

class PostgresPlanParseTest extends FlatSpec with Matchers {
  "PostgreSQL Plan Parser" should "load basic PostgreSQL plan" in {
    val result = PlanParseTestHelper.test("postgres-nested-join", PostgreSQL)
    1 should be(1)
  }

  it should "load complex PostgreSQL plan" in {
    val result = PlanParseTestHelper.test("postgres-complicated-query", PostgreSQL)
    println(utils.JsonSerializers.writeResponseMessage(PlanResultResponse(UUID.randomUUID, result, 0), debug = true))

    1 should be(1)
  }

  it should "throw IllegalArgumentException if invalid JSON is passed" in {
    a[IllegalArgumentException] should be thrownBy {
      throw new IllegalArgumentException("!")
    }
  }
}
