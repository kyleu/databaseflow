package models.database

import java.sql.{ PreparedStatement, ResultSet }

trait RawQuery[A] {
  def sql: String
  def values: Seq[Any] = Seq.empty
  def handle(stmt: PreparedStatement, results: ResultSet): A
}

trait Query[A] extends RawQuery[A] {
  override def handle(stmt: PreparedStatement, results: ResultSet) = reduce(stmt, new Row.Iter(results))
  def reduce(stmt: PreparedStatement, rows: Iterator[Row]): A
}

trait SingleRowQuery[A] extends Query[A] {
  def map(row: Row): A
  override final def reduce(stmt: PreparedStatement, rows: Iterator[Row]) = if (rows.hasNext) {
    rows.map(map).next()
  } else {
    throw new IllegalStateException(s"No row returned for [$sql].")
  }
}

trait FlatSingleRowQuery[A] extends Query[Option[A]] {
  def flatMap(row: Row): Option[A]
  override final def reduce(stmt: PreparedStatement, rows: Iterator[Row]) = if (rows.hasNext) { flatMap(rows.next()) } else { None }
}
