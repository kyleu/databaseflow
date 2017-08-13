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
    ignored: Boolean = false
) {
  val className = ExportHelper.toClassName(propertyName)

  val graphQlArgType = {
    val argTypeRaw = t match {
      case StringType => "StringType"
      case BigDecimalType => "BigDecimalType"
      case BooleanType => "BooleanType"
      case ByteType => "IntType"
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

      case UnknownType => "UnknownType"
    }
    if (notNull) { argTypeRaw } else { "OptionInputType(" + argTypeRaw + ")" }
  }
}
