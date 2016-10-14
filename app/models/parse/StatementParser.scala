package models.parse

import fastparse.all._
import utils.Logging

object StatementParser extends Logging {
  def sourceFor(sql: String) = singleTable(sql) match {
    case Right(x) => Some(x)
    case Left(_) => None
  }

  private[this] val optWs = P(" " | "\n" | "\t").rep(min = 0)
  private[this] val ws = P(" " | "\n" | "\t").rep(min = 1)
  private[this] val select = P(IgnoreCase("select"))
  private[this] val wildcard = P("*")
  private[this] val from = P(IgnoreCase("from"))
  private[this] val leftQuote = P("\"" | "`" | "[").rep(min = 0, max = 1)
  private[this] val tableName = P(CharPred(c => c != '\"' && c != '`' && c != ']' && c != ' ' && c != '\n')).rep(1).!
  private[this] val rightQuote = P("\"" | "`" | "]").rep(min = 0, max = 1)

  private[this] val sqlParser = P(optWs ~ select ~ ws ~ wildcard ~ ws ~ from ~ ws ~ leftQuote ~ tableName ~ rightQuote ~ optWs ~ End)

  def singleTable(sql: String) = sqlParser.parse(sql) match {
    case Parsed.Success(table, _) => Right(table)
    case Parsed.Failure(_, idx, extra) => Left(s"Cannot parse [$sql]: Error at idx [$idx] - $extra")
  }
}
