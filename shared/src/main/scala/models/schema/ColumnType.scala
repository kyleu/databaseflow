package models.schema

import java.util.UUID

import enumeratum._

sealed abstract class ColumnType(val key: String) extends EnumEntry {
  def fromString(s: String): Any = s
  override def toString = key
}

object ColumnType extends Enum[ColumnType] {
  case object StringType extends ColumnType("string")
  case object BigDecimalType extends ColumnType("decimal") {
    override def fromString(s: String) = BigDecimal(s)
  }
  case object BooleanType extends ColumnType("boolean") {
    override def fromString(s: String) = s == "true" || s == "1" || s == "yes"
  }
  case object ByteType extends ColumnType("byte") {
    override def fromString(s: String) = s.toByte
  }
  case object ShortType extends ColumnType("short") {
    override def fromString(s: String) = s.toShort
  }
  case object IntegerType extends ColumnType("integer") {
    override def fromString(s: String) = s.toInt
  }
  case object LongType extends ColumnType("long") {
    override def fromString(s: String) = s.toLong
  }
  case object FloatType extends ColumnType("float") {
    override def fromString(s: String) = s.toFloat
  }
  case object DoubleType extends ColumnType("double") {
    override def fromString(s: String) = s.toDouble
  }
  case object ByteArrayType extends ColumnType("bytearray")
  case object DateType extends ColumnType("date")
  case object TimeType extends ColumnType("time")
  case object TimestampType extends ColumnType("timestamp")
  case object RefType extends ColumnType("ref")
  case object XmlType extends ColumnType("xml")
  case object UuidType extends ColumnType("uuid") {
    override def fromString(s: String) = UUID.fromString(s)
  }

  case object NullType extends ColumnType("null") {
    override def fromString(s: String) = None.orNull
  }
  case object ObjectType extends ColumnType("object")
  case object StructType extends ColumnType("struct")
  case object ArrayType extends ColumnType("array")

  case object UnknownType extends ColumnType("unknown")

  override val values = findValues
}
