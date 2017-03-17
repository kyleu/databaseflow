package models.graphql

import models.schema.{Column, ColumnType}
import sangria.schema._

object ColumnGraphQL {
  private[this] def getColumnType(t: ColumnType) = t match {
    case ColumnType.StringType => StringType
    case ColumnType.BigDecimalType => BigDecimalType
    case ColumnType.BooleanType => BooleanType
    case ColumnType.ByteType => IntType
    case ColumnType.ShortType => IntType
    case ColumnType.IntegerType => IntType
    case ColumnType.LongType => LongType
    case ColumnType.FloatType => FloatType
    case ColumnType.DoubleType => BigDecimalType
    case ColumnType.ByteArrayType => StringType
    case ColumnType.DateType => StringType
    case ColumnType.TimeType => StringType
    case ColumnType.TimestampType => StringType

    case ColumnType.RefType => StringType
    case ColumnType.XmlType => StringType
    case ColumnType.UuidType => StringType

    case ColumnType.NullType => StringType
    case ColumnType.ObjectType => StringType
    case ColumnType.StructType => StringType
    case ColumnType.ArrayType => StringType

    case ColumnType.UnknownType => StringType

    case _ => StringType
  }

  private[this] def getData(name: String, t: ColumnType) = t match {
    case ColumnType.StringType => s"$name (String)"
    case ColumnType.BigDecimalType => BigDecimal(0)
    case ColumnType.BooleanType => true
    case ColumnType.ByteType => 0
    case ColumnType.ShortType => 0
    case ColumnType.IntegerType => 0
    case ColumnType.LongType => 0L
    case ColumnType.FloatType => 0F
    case ColumnType.DoubleType => BigDecimal(0)
    case ColumnType.ByteArrayType => s"$name (ByteArray)"
    case ColumnType.DateType => s"$name (Date)"
    case ColumnType.TimeType => s"$name (Time)"
    case ColumnType.TimestampType => s"$name (Timestamp)"

    case ColumnType.RefType => s"$name (Ref)"
    case ColumnType.XmlType => s"$name (XML)"
    case ColumnType.UuidType => s"$name (UUID)"

    case ColumnType.NullType => s"$name (Null)"
    case ColumnType.ObjectType => s"$name (Object)"
    case ColumnType.StructType => s"$name (Struct)"
    case ColumnType.ArrayType => s"$name (Array)"

    case ColumnType.UnknownType => s"$name (Unknown)"

    case _ => StringType
  }

  def getColumnField[T](col: Column) = Field(
    name = col.name.replaceAllLiterally(" ", ""),
    fieldType = StringType,
    description = col.description,
    resolve = (x: Context[GraphQLContext, T]) => getData(col.name, col.columnType).toString
  )
}
