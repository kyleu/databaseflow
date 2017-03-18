package models.graphql

import models.schema.{Column, ColumnType}

object Resultset {
  def mock(columns: Seq[Column], rows: Int) = {
    val colNames = columns.map(_.name)
    (0 until rows).map { _ =>
      val data = columns.map { col =>
        col.columnType match {
          case ColumnType.StringType => Some(col.name)
          case ColumnType.BigDecimalType | ColumnType.DoubleType => Some("0.1")
          case ColumnType.BooleanType => Some("true")
          case ColumnType.ByteType | ColumnType.ShortType | ColumnType.IntegerType => Some("0")
          case ColumnType.LongType => Some("0")
          case ColumnType.FloatType => Some("0.1")
          case ColumnType.ByteArrayType => Some(col.name)
          case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => Some(col.name)
          case ColumnType.RefType | ColumnType.XmlType | ColumnType.UuidType => Some(col.name)
          case ColumnType.NullType | ColumnType.ObjectType | ColumnType.StructType | ColumnType.ArrayType => Some(col.name)
          case ColumnType.UnknownType => Some(col.name)
        }
      }
      Resultset(colNames, data)
    }
  }
}

case class Resultset(columns: Seq[String], data: Seq[Option[String]]) {
  private[this] val columnIndexes = columns.zipWithIndex.toMap
  private[this] def colIndex(col: String) = columnIndexes.getOrElse(col, throw new IllegalStateException(s"Invalid column [$col]."))

  def getCell(col: String) = data(colIndex(col))
  def getRequiredCell(col: String) = getCell(col).getOrElse(throw new IllegalStateException(s"Null value for column [$col]."))
}
