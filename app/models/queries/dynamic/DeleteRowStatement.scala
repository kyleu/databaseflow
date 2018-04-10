package models.queries.dynamic

import models.database.Statement
import models.engine.DatabaseEngine
import models.schema.Column

case class DeleteRowStatement(
  name: String, pk: Seq[(String, String)], columns: Seq[Column], engine: DatabaseEngine
) extends Statement {
  private[this] def quote(s: String) = engine.cap.leftQuote + s + engine.cap.rightQuote

  private[this] val pkColumns = columns.flatMap(col => pk.find(_._1 == col.name).map(col -> _._2))
  private[this] val pkValues = pkColumns.map(x => ColumnValueParser.parse(x._1.columnType, x._2))
  private[this] val pkClause = pkColumns.zip(pkValues).map { x =>
    val placeholder = x._2 match {
      case Right(_) => "?"
      case Left(ex) => ex
    }
    s"${quote(x._1._1.name)} = $placeholder"
  }.mkString(" and ")

  override val sql = s"delete from ${quote(name)} where $pkClause"

  override val values: Seq[Any] = pkValues.flatMap {
    case Right(x) => Some(x)
    case Left(_) => None
  }
}
