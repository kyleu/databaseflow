package services.scalaexport.config

import models.schema.ColumnType
import models.schema.ColumnType._
import services.scalaexport.ExportHelper

case class ExportField(
    columnName: String,
    propertyName: String,
    title: String,
    description: Option[String],
    t: ColumnType,
    sqlTypeName: String,
    defaultValue: Option[String],
    notNull: Boolean = false,
    inSearch: Boolean = false,
    inView: Boolean = true,
    inSummary: Boolean = false,
    ignored: Boolean = false
) {
  val nullable = !notNull

  val className = ExportHelper.toClassName(propertyName)
  def classNameForSqlType(enums: Seq[ExportEnum]) = t match {
    case EnumType => enums.find(_.name == sqlTypeName).map { e =>
      s"EnumType(${e.className})"
    }.getOrElse(throw new IllegalStateException(s"Cannot find enum matching [$sqlTypeName]."))
    case ArrayType => ArrayType.typForSqlType(sqlTypeName)
    case _ => t.className
  }

  lazy val defaultString = t match {
    case BooleanType => defaultValue.map(v => if (v == "1" || v == "true") { "true" } else { "false" }).getOrElse("false")
    case ByteType => defaultValue.filter(_.matches("[0-9]+")).getOrElse("0")
    case IntegerType => defaultValue.filter(_.matches("[0-9]+")).getOrElse("0")
    case LongType => defaultValue.filter(_.matches("[0-9]+")).getOrElse("0") + "L"
    case ShortType => defaultValue.filter(_.matches("[0-9]+")).getOrElse("0") + ".toShort"
    case FloatType => defaultValue.filter(_.matches("[0-9\\.]+")).getOrElse("0.0") + "f"
    case DoubleType => defaultValue.filter(_.matches("[0-9\\.]+")).getOrElse("0.0")
    case BigDecimalType => s"BigDecimal(${defaultValue.filter(_.matches("[0-9\\.]+")).getOrElse("0")})"

    case DateType => "util.DateUtils.today"
    case TimeType => "util.DateUtils.currentTime"
    case TimestampType => "util.DateUtils.now"

    case UuidType => defaultValue.filter(_.length == 36).map(d => s"""UUID.fromString("$d")""").getOrElse("UUID.randomUUID")

    case JsonType => "util.JsonSerializers.emptyObject"
    case ArrayType => "Seq.empty"
    case TagsType => "Seq.empty[models.tag.Tag]"
    case EnumType => "\"" + defaultValue.getOrElse("") + "\""

    case _ => "\"" + defaultValue.getOrElse("") + "\""
  }

  def fromString(s: String) = t.fromString.replaceAllLiterally("xxx", s)

  private[this] val graphQLType = t match {
    case StringType => "StringType"

    case BooleanType => "BooleanType"
    case ByteType => "byteType"
    case ShortType => "IntType"
    case IntegerType => "IntType"
    case LongType => "LongType"
    case FloatType => "FloatType"
    case DoubleType => "DoubleType"
    case BigDecimalType => "BigDecimalType"

    case DateType => "localDateType"
    case TimeType => "localTimeType"
    case TimestampType => "localDateTimeType"

    case RefType => "StringType"
    case XmlType => "StringType"
    case UuidType => "uuidType"

    case ObjectType => "StringType"
    case StructType => "StringType"
    case JsonType => "JsonType"

    case EnumType => "StringType"
    case CodeType => "StringType"
    case TagsType => "TagsType"

    case ByteArrayType => "ArrayType(StringType)"
    case ArrayType => sqlTypeName match {
      case x if x.startsWith("_int") => "ArrayType(IntType)"
      case x if x.startsWith("_uuid") => "ArrayType(uuidType)"
      case _ => "ArrayType(StringType)"
    }

    case UnknownType => "UnknownType"
  }

  val graphQlArgType = if (notNull) { graphQLType } else { "OptionInputType(" + graphQLType + ")" }
}
