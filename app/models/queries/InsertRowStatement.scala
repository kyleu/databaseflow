package models.queries

import models.database.Statement
import models.engine.DatabaseEngine
import models.schema.Column

import scala.util.control.NonFatal

case class InsertRowStatement(name: String, params: Map[String, String], columns: Seq[Column], engine: DatabaseEngine) extends Statement {
  private[this] def quote(s: String) = engine.cap.leftQuote + s + engine.cap.rightQuote

  private[this] val activeColumns = columns.flatMap { col =>
    params.get(col.name).map(col -> _)
  }

  private[this] val parsedValues = activeColumns.map { x =>
    try {
      Right(ColumnValueParser.fromString(x._1.columnType, x._2))
    } catch {
      case NonFatal(_) => Left(x._2)
    }
  }

  override val sql = {
    val columns = activeColumns.map(c => quote(c._1.name)).mkString(", ")
    val placeholders = parsedValues.map {
      case Right(_) => "?"
      case Left(x) => x
    }.mkString(", ")
    s"insert into ${quote(name)} ($columns) values ($placeholders)"
  }

  override val values: Seq[Any] = parsedValues.flatMap {
    case Right(x) => Some(x)
    case Left(_) => None
  }
}
