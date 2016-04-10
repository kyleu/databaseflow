package utils

import java.sql.{ ResultSet, Timestamp }

import models.database.Row
import org.joda.time.LocalDateTime
import org.postgresql.jdbc.PgArray

object JdbcUtils {
  def toLocalDateTime(row: Row, column: String) = {
    val ts = row.as[Timestamp](column)
    new LocalDateTime(ts.getTime)
  }

  def toSeq[T](row: Row, column: String): Seq[Any] = {
    val a = row.as[PgArray](column)
    a.getArray.asInstanceOf[Array[T]].toSeq
  }

  def resultsetToString(rs: ResultSet, indent: Int = 0, showColumns: Boolean = true) = {
    val whitespace = (0 until indent).map(x => " ").mkString
    val ret = collection.mutable.ArrayBuffer.empty[String]
    val columns = rs.getMetaData
    while (rs.next()) {
      val row = (1 to columns.getColumnCount).map(i => Option(rs.getObject(i)).map(_.toString).getOrElse("-null-"))
      ret += (whitespace + row.mkString(", "))
    }
    if (showColumns) {
      val cols = (1 to columns.getColumnCount).map(columns.getColumnLabel)
      val colsLabel = whitespace + cols.map(x => "[" + x + "]").mkString(", ")
      colsLabel + "\n" + ret.mkString("\n")
    } else {
      ret.mkString("\n")
    }
  }

  def rowToString(row: Row, indent: Int = 0, showColumns: Boolean = true) = {
    val whitespace = (0 until indent).map(x => " ").mkString
    val ret = collection.mutable.ArrayBuffer.empty[String]
    val columns = row.rs.getMetaData
    val columnLabels = (1 to columns.getColumnCount).map(columns.getColumnLabel)
    val result = (1 to columns.getColumnCount).map(i => Option(row.rs.getObject(i)).map(_.toString).getOrElse("-null-"))
    columnLabels.zip(result).map(x => whitespace + x._1 + ": " + x._2).mkString("\n")
  }
}
