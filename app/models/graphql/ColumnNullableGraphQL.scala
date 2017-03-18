package models.graphql

import models.schema.{Column, ColumnType}
import sangria.schema._

object ColumnNullableGraphQL {
  def getColumnField(col: Column) = {
    if (col.notNull) {
      throw new IllegalStateException(s"Not-null column [${col.name}] passed to nullable method.")
    }
    val cleanName = col.name.replaceAllLiterally(" ", "")
    col.columnType match {
      case ColumnType.StringType => Field(
        name = cleanName,
        fieldType = OptionType(StringType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, Resultset]) => Some(s"$cleanName (String)")
      )
      case ColumnType.BigDecimalType | ColumnType.DoubleType => Field(
        name = cleanName,
        fieldType = OptionType(BigDecimalType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, Resultset]) => Some(BigDecimal(0))
      )
      case ColumnType.BooleanType => Field(
        name = cleanName,
        fieldType = OptionType(BooleanType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, Resultset]) => Some(true)
      )
      case ColumnType.ByteType | ColumnType.ShortType | ColumnType.IntegerType => Field(
        name = cleanName,
        fieldType = OptionType(IntType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, Resultset]) => Some(0)
      )
      case ColumnType.LongType => Field(
        name = cleanName,
        fieldType = OptionType(LongType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, Resultset]) => Some(0L)
      )
      case ColumnType.FloatType => Field(
        name = cleanName,
        fieldType = OptionType(FloatType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, Resultset]) => Some(0.0)
      )
      case ColumnType.ByteArrayType => Field(
        name = cleanName,
        fieldType = OptionType(StringType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, Resultset]) => Some(s"$cleanName (ByteArray)")
      )
      case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => Field(
        name = cleanName,
        fieldType = OptionType(StringType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, Resultset]) => Some(s"$cleanName (${col.columnType})")
      )

      case ColumnType.RefType | ColumnType.XmlType | ColumnType.UuidType => Field(
        name = cleanName,
        fieldType = OptionType(StringType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, Resultset]) => Some(s"$cleanName (${col.columnType})")
      )

      case ColumnType.NullType | ColumnType.ObjectType | ColumnType.StructType | ColumnType.ArrayType => Field(
        name = cleanName,
        fieldType = OptionType(StringType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, Resultset]) => Some(s"$cleanName (${col.columnType})")
      )

      case ColumnType.UnknownType => Field(
        name = cleanName,
        fieldType = OptionType(StringType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, Resultset]) => Some(s"$cleanName (${col.columnType})")
      )
    }
  }
}
