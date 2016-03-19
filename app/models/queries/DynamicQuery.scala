package models.queries

import models.QueryResult
import models.database.{ Row, Query }

case class DynamicQuery(override val sql: String) extends Query[(Seq[QueryResult.Col], Seq[Seq[Option[String]]])] {
  override def reduce(rows: Iterator[Row]) = {
    var columns: Option[Seq[QueryResult.Col]] = None
    val data = rows.zipWithIndex.map { row =>
      val md = row._1.rs.getMetaData
      val cc = md.getColumnCount

      if (columns.isEmpty) {
        columns = Some((1 until cc).map(i => QueryResult.Col(md.getColumnLabel(i), md.getColumnTypeName(i))))
      }

      (1 until cc).map(i => row._1.asOpt[Any](i).map(_.toString))
    }.toList

    columns.getOrElse(Nil) -> data
  }
}
