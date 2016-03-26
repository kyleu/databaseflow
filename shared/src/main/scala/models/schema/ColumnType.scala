package models.schema

import enumeratum._

sealed abstract class ColumnType(val key: String) extends EnumEntry {
  override def toString = key
}

object ColumnType extends Enum[ColumnType] {
  case object StringType extends ColumnType("string")
  case object BigDecimalType extends ColumnType("decimal")
  case object BooleanType extends ColumnType("boolean")
  case object ByteType extends ColumnType("byte")
  case object ShortType extends ColumnType("short")
  case object IntegerType extends ColumnType("integer")
  case object LongType extends ColumnType("long")
  case object FloatType extends ColumnType("float")
  case object DoubleType extends ColumnType("double")
  case object ByteArrayType extends ColumnType("bytearray")
  case object DateType extends ColumnType("date")
  case object TimeType extends ColumnType("time")
  case object TimestampType extends ColumnType("timestamp")
  case object RefType extends ColumnType("ref")
  case object XmlType extends ColumnType("xml")
  case object UuidType extends ColumnType("uuid")

  case object NullType extends ColumnType("null")
  case object ObjectType extends ColumnType("object")
  case object StructType extends ColumnType("struct")
  case object ArrayType extends ColumnType("array")

  case object UnknownType extends ColumnType("unknown")

  override def values = findValues
}
