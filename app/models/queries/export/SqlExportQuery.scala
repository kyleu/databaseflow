package models.queries.export

import java.io.{OutputStream, PrintWriter}

import models.database.{Query, Row}
import models.engine.DatabaseEngine
import models.queries.dynamic.DynamicQuery
import models.query.QueryResult
import models.schema.ColumnType

case class SqlExportQuery(
    override val sql: String, override val values: Seq[Any], source: QueryResult.Source, engine: DatabaseEngine, out: OutputStream
) extends Query[Unit] {
  private[this] val tableName = engine.cap.leftQuote + (source.t match {
    case QueryResult.SourceType.Table => source.name
    case QueryResult.SourceType.View => source.name
    case QueryResult.SourceType.Cache => "table_name"
  }) + engine.cap.rightQuote

  private[this] def forCol(x: ColumnType, d: Option[String]) = d match {
    case Some(data) => s"'$data'"
    case None => "null"
  }

  private[this] def sqlFor(data: Seq[Option[String]], columns: Seq[(String, ColumnType)], columnsStr: String) = {
    s"insert into $tableName ($columnsStr) values (${columns.zip(data).map(x => forCol(x._1._2, x._2)).mkString(", ")});"
  }

  override def reduce(rows: Iterator[Row]) = {
    val writer = new PrintWriter(out)
    if (rows.hasNext) {
      val firstRow = rows.next()
      val md = firstRow.rs.getMetaData
      val cc = md.getColumnCount
      val columns = (1 to cc).map(i => md.getColumnLabel(i) -> DynamicQuery.getColumnMetadata(md, i)._1)
      val columnsStr = columns.map(c => engine.cap.leftQuote + c._1 + engine.cap.rightQuote).mkString(", ")
      val firstRowData = (1 to cc).map(i => firstRow.asOpt[Any](i).map(_.toString))
      writer.println(sqlFor(firstRowData, columns, columnsStr))

      rows.foreach { row =>
        val data = (1 to cc).map(i => row.asOpt[Any](i).map(_.toString))
        writer.println(sqlFor(data, columns, columnsStr))
      }
    }
    writer.close()
  }
}
