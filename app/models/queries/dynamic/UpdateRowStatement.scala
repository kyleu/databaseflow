package models.queries.dynamic

import models.database.Statement
import models.engine.DatabaseEngine
import models.query.ColumnValueParser
import models.schema.Column

case class UpdateRowStatement(
    name: String, pk: Seq[(String, String)], params: Map[String, String], columns: Seq[Column], engine: DatabaseEngine
) extends Statement {
  private[this] def quote(s: String) = engine.cap.leftQuote + s + engine.cap.rightQuote
  private[this] val activeColumns = columns.flatMap(col => params.get(col.name).map(col -> _))
  private[this] val parsedValues = activeColumns.map(x => ColumnValueParser.parse(x._1.columnType, x._2))

  override val sql = {
    val setClause = activeColumns.zip(parsedValues).map { c =>
      val placeholder = c._2 match {
        case Right(_) => "?"
        case Left(x) => x
      }
      s"${quote(c._1._1.name)} = $placeholder"
    }.mkString(", ")
    val pkClause = "1 = 1"
    s"update ${quote(name)} set $setClause where $pkClause"
  }

  override val values: Seq[Any] = parsedValues.flatMap {
    case Right(x) => Some(x)
    case Left(_) => None
  }
}
