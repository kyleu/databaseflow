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
  val className = ExportHelper.toClassName(propertyName)

  lazy val defaultString = t match {
    case ColumnType.BooleanType => defaultValue.map(v => if (v == "1" || v == "true") { "true" } else { "false" }).getOrElse("false")
    case ColumnType.ByteType => defaultValue.getOrElse("0")
    case ColumnType.IntegerType => defaultValue.getOrElse("0")
    case ColumnType.LongType => defaultValue.getOrElse("0") + "L"
    case ColumnType.ShortType => defaultValue.getOrElse("0") + ".toShort"
    case ColumnType.FloatType => defaultValue.getOrElse("0.0") + "f"
    case ColumnType.DoubleType => defaultValue.getOrElse("0.0")
    case ColumnType.BigDecimalType => s"BigDecimal(${defaultValue.getOrElse("0")})"
    case ColumnType.UuidType => defaultValue.map(d => s"UUID.fromString($d)").getOrElse("UUID.randomUUID")
    case ColumnType.TimestampType => "util.DateUtils.now"
    case ColumnType.DateType => "util.DateUtils.today"
    case ColumnType.TimeType => "util.DateUtils.currentTime"
    case ColumnType.TagsType => "Seq.empty[models.tag.Tag]"
    case _ => "\"" + defaultValue.getOrElse("") + "\""
  }

  def fromString(s: String) = t.fromString.replaceAllLiterally("xxx", s)

  private[this] val graphQLType = t match {
    case StringType => "StringType"
    case BigDecimalType => "BigDecimalType"
    case BooleanType => "BooleanType"
    case ByteType => "byteType"
    case ShortType => "IntType"
    case IntegerType => "IntType"
    case LongType => "LongType"
    case FloatType => "FloatType"
    case DoubleType => "DoubleType"
    case ByteArrayType => "ArrayType(StringType)"
    case DateType => "localDateType"
    case TimeType => "localTimeType"
    case TimestampType => "localDateTimeType"

    case RefType => "StringType"
    case XmlType => "StringType"
    case UuidType => "uuidType"

    case ObjectType => "StringType"
    case StructType => "StringType"
    case ArrayType => "ArrayType(StringType)"
    case TagsType => "TagsType"

    case UnknownType => "UnknownType"
  }

  val graphQlArgType = if (notNull) { graphQLType } else { "OptionInputType(" + graphQLType + ")" }
}
