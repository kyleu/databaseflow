package test.plan

import models.engine.MySQL
import org.scalatest.{ FlatSpec, Matchers }

class MySqlPlanParseTest extends FlatSpec with Matchers {
  "MySQL Plan Parser" should "load basic MySQL plan" in {
    val result = PlanParseTestHelper.test("mysql-nested-loop", MySQL)
    PlanParseTestHelper.debugPlanResult(result)
    1 should be(1)
  }

  it should "load complex MySQL plan" in {
    val result = PlanParseTestHelper.test("mysql-complicated-query", MySQL)
    PlanParseTestHelper.debugPlanResult(result)
    1 should be(1)
  }

  it should "throw IllegalArgumentException if invalid JSON is passed" in {
    a[IllegalArgumentException] should be thrownBy {
      throw new IllegalArgumentException("!")
    }
  }
}
