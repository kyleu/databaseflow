package models.engine

import java.util.UUID

import models.schema.ColumnType
import models.schema.ColumnType._

import scala.util.control.NonFatal

object EngineColumnParser {
  def parse(t: ColumnType, v: String) = try {
    Right(fromString(t, v))
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
    case DateType => s
    case TimeType => s
    case TimestampType => s
    case UuidType => UUID.fromString(s)
    case _ => s
  }
}
