package models.queries.dynamic

import models.database.Statement
import models.engine.DatabaseEngine
import models.schema.Column

case class UpdateRowStatement(
    name: String, pk: Seq[(String, String)], params: Map[String, String], columns: Seq[Column], engine: DatabaseEngine
) extends Statement {
  private[this] def quote(s: String) = engine.cap.leftQuote + s + engine.cap.rightQuote

  private[this] val activeColumns = columns.flatMap(col => params.get(col.name).map(col -> _))
  private[this] val parsedValues = activeColumns.map(x => ColumnValueParser.parse(x._1.columnType, x._2))
  private[this] val setClause = activeColumns.zip(parsedValues).map { c =>
    val placeholder = c._2 match {
      case Right(_) => "?"
      case Left(x) => x
    }
    s"${quote(c._1._1.name)} = $placeholder"
  }.mkString(", ")

  private[this] val pkColumns = columns.flatMap(col => pk.find(_._1 == col.name).map(col -> _._2))
  private[this] val pkValues = pkColumns.map(x => ColumnValueParser.parse(x._1.columnType, x._2))
  private[this] val pkClause = pkColumns.zip(pkValues).map { x =>
    val placeholder = x._2 match {
      case Right(_) => "?"
      case Left(ex) => ex
    }
    s"${quote(x._1._1.name)} = $placeholder"
  }.mkString(", ")

  override val sql = {
    s"update ${quote(name)} set $setClause where $pkClause"
  }

  override val values: Seq[Any] = {
    val insertVals = parsedValues.flatMap {
      case Right(x) => Some(x)
      case Left(_) => None
    }
    val pkVals = pkValues.flatMap {
      case Right(x) => Some(x)
      case Left(_) => None
    }
    insertVals ++ pkVals
  }
}
