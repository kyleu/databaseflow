package models.schema

import enumeratum._

sealed abstract class ColumnType(
  val key: String,
  val asScala: String,
  val fromString: String,
  val requiredImport: Option[String] = None,
  val isNumeric: Boolean = false
) extends EnumEntry {
  val asScalaFull = requiredImport match {
    case Some(pkg) => pkg + "." + asScala
    case None => asScala
  }

  val className = getClass.getSimpleName.stripSuffix("$")

  override def toString = key
}

object ColumnType extends Enum[ColumnType] {
  case object StringType extends ColumnType("string", "String", "xxx")
  case object BigDecimalType extends ColumnType("decimal", "BigDecimal", "BigDecimal(xxx)", isNumeric = true)
  case object BooleanType extends ColumnType("boolean", "Boolean", "xxx == \"true\"")
  case object ByteType extends ColumnType("byte", "Byte", "xxx.toInt.toByte")
  case object ShortType extends ColumnType("short", "Short", "xxx.toInt.toShort", isNumeric = true)
  case object IntegerType extends ColumnType("integer", "Int", "xxx.toInt", isNumeric = true)
  case object LongType extends ColumnType("long", "Long", "xxx.toLong", isNumeric = true)
  case object FloatType extends ColumnType("float", "Float", "xxx.toFloat", isNumeric = true)
  case object DoubleType extends ColumnType("double", "Double", "xxx.toDouble", isNumeric = true)
  case object ByteArrayType extends ColumnType("bytearray", "xxx.split(\",\").map(_.toInt.toByte)", "Array[Byte]")
  case object DateType extends ColumnType("date", "LocalDate", "util.DateUtils.fromDateString(xxx)", requiredImport = Some("java.time"))
  case object TimeType extends ColumnType("time", "LocalTime", "util.DateUtils.fromTimeString(xxx)", requiredImport = Some("java.time"))
  case object TimestampType extends ColumnType("timestamp", "LocalDateTime", "util.DateUtils.fromIsoString(xxx)", requiredImport = Some("java.time"))

  case object RefType extends ColumnType("ref", "String", "xxx")
  case object XmlType extends ColumnType("xml", "String", "xxx")
  case object UuidType extends ColumnType("uuid", "UUID", "UUID.fromString(xxx)", requiredImport = Some("java.util"))

  case object ObjectType extends ColumnType("object", "String", "xxx")
  case object StructType extends ColumnType("struct", "String", "xxx")
  case object ArrayType extends ColumnType("array", "Array[Any]", "xxx.split(\",\")") {
    def forSqlType(s: String) = s match {
      case _ if s.startsWith("_int") => "Seq[Long]"
      case _ => "Seq[String]"
    }
  }

  case object UnknownType extends ColumnType("unknown", "Any", "xxx")

  override val values = findValues
}
