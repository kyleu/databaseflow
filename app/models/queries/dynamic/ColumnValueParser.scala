package models.queries.dynamic

import java.util.UUID

import models.schema.ColumnType
import models.schema.ColumnType._
import utils.DateUtils

import scala.util.control.NonFatal

object ColumnValueParser {
  def parse(t: ColumnType, v: String) = try {
    Right(ColumnValueParser.fromString(t, v))
  } catch {
    case NonFatal(_) => Left(v)
  }

  def fromString(t: ColumnType, s: String): Any = t match {
    case BigDecimalType => BigDecimal(s)
    case BooleanType => s == "true" || s == "1" || s == "yes"
    case ByteType => s.toByte
    case ShortType => s.toShort
    case IntegerType => s.toInt
    case LongType => s.toLong
    case FloatType => s.toFloat
    case DoubleType => s.toDouble
    case ByteArrayType => s.getBytes
    case DateType => DateUtils.sqlDateFromString(s)
    case TimeType => DateUtils.sqlTimeFromString(s)
    case TimestampType => DateUtils.sqlDateTimeFromString(s)
    case UuidType => UUID.fromString(s)
    case _ => s
  }
}
