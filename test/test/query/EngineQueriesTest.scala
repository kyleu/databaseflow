package test.query

import models.engine.DatabaseEngine.{MySQL, PostgreSQL, SQLServer}
import models.engine.EngineQueries
import models.query.RowDataOptions
import models.schema.FilterOp
import org.scalatest.{FlatSpec, Matchers}

class EngineQueriesTest extends FlatSpec with Matchers {
  val rdoLimit = RowDataOptions(
    limit = Some(1000)
  )

  val rdoCol = RowDataOptions(
    filterCol = Some("y"),
    filterOp = Some(FilterOp.Equal),
    filterVal = Some("z")
  )

  val rdoOrder = RowDataOptions(
    orderByCol = Some("y")
  )

  "Engine Queries" should "output SQL Server limit syntax" in {
    val sql = EngineQueries.selectFrom("X", rdoLimit)(SQLServer)
    sql should be("select * from [X] offset 0 rows fetch next 1000 rows only")
  }

  it should "output PostgreSQL limit syntax" in {
    val sql = EngineQueries.selectFrom("X", rdoLimit)(PostgreSQL)
    sql should be("select * from \"X\" limit 1000")
  }

  it should "output MySQL limit syntax" in {
    val sql = EngineQueries.selectFrom("X", rdoLimit)(MySQL)
    sql should be("select * from `X` limit 1000")
  }

  it should "output SQL Server where clause syntax" in {
    val sql = EngineQueries.selectFrom("X", rdoCol)(SQLServer)
    sql should be("select * from [X] where [y] = 'z'")
  }

  it should "output PostgreSQL where clause syntax" in {
    val sql = EngineQueries.selectFrom("X", rdoCol)(PostgreSQL)
    sql should be("select * from \"X\" where \"y\" = 'z'")
  }

  it should "output a proper order by clause" in {
    val sql = EngineQueries.selectFrom("X", rdoOrder)(PostgreSQL)
    sql should be("select * from \"X\" order by \"y\" asc")
  }
}
