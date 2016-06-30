package test.plan

import models.query.SqlParser
import org.scalatest.{FlatSpec, Matchers}

object SqlParserTest {
  val testQueryOne = "select * from foo"
  val testQueryTwo = "select * from bar"
  val testQueryThree = "select * from baz"
  val testQueryQuoted = "select 'foo', `bar`, \"baz\" from table"
  val testQuerySquareBracket = "select [foo] from [table]"
  val testQueryLineBreaks = "select foo\n\nfrom table"
  val testQueryDashComment = "-- Comment with a ;\nselect foo from table"
  val testQueryHashComment = "# Comment with a ;\nselect foo from table"
  val testQueryCStyleComment = "/* Comment with a ; */\nselect foo from table"
  val testQueryTricky = "select\n  'foo;bar',\n  `x;y` -- ;\n  /*;*/\nfrom\n  [table]"
}

class SqlParserTest extends FlatSpec with Matchers {
  import SqlParserTest._

  "SQL Parser" should "handle an empty string" in {
    val result = SqlParser.split("")
    result.length should be(0)
  }

  it should "parse a single statement" in {
    val result = SqlParser.split(testQueryOne + ";")
    result.length should be(1)
    result.head._1 should be(testQueryOne)
  }

  it should "parse multiple basic statements" in {
    val result = SqlParser.split(testQueryOne + ";\n" + testQueryTwo + ";\n" + testQueryThree)

    result.length should be(3)
    result.head._1 should be(testQueryOne)
    result(1)._1 should be(testQueryTwo)
    result(2)._1 should be(testQueryThree)
  }

  it should "parse a quoted statement" in {
    val result = SqlParser.split(testQueryQuoted)
    result.length should be(1)
    result.head._1 should be(testQueryQuoted)
  }

  it should "parse a square bracketed statement" in {
    val result = SqlParser.split(testQuerySquareBracket)
    result.length should be(1)
    result.head._1 should be(testQuerySquareBracket)
  }

  it should "parse a statement with line breaks" in {
    val result = SqlParser.split(testQueryLineBreaks)
    result.length should be(1)
    result.head._1 should be(testQueryLineBreaks)
  }

  it should "parse a statement with dash comments" in {
    val result = SqlParser.split(testQueryDashComment)
    result.length should be(1)
    result.head._1 should be(testQueryDashComment)
  }

  it should "parse a statement with hash comments" in {
    val result = SqlParser.split(testQueryHashComment)
    result.length should be(1)
    result.head._1 should be(testQueryHashComment)
  }

  it should "parse a statement with C style comments" in {
    val result = SqlParser.split(testQueryCStyleComment)
    result.length should be(1)
    result.head._1 should be(testQueryCStyleComment)
  }

  it should "parse a tricky statement" in {
    val result = SqlParser.split(testQueryTricky)
    result.length should be(1)
    result.head._1 should be(testQueryTricky)
  }
}
