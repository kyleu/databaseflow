package models.graphql

import models.schema.{Column, ColumnType}
import sangria.schema._

object ColumnNotNullGraphQL {
  def getColumnField[T](col: Column) = {
    if (!col.notNull) {
      throw new IllegalStateException(s"Nullable column [${col.name}] passed to non-nullable method.")
    }
    val cleanName = col.name.replaceAllLiterally(" ", "")
    col.columnType match {
      case ColumnType.StringType => Field(
        name = cleanName,
        fieldType = StringType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, T]) => s"$cleanName (String)"
      )
      case ColumnType.BigDecimalType | ColumnType.DoubleType => Field(
        name = cleanName,
        fieldType = BigDecimalType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, T]) => BigDecimal(0)
      )
      case ColumnType.BooleanType => Field(
        name = cleanName,
        fieldType = BooleanType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, T]) => true
      )
      case ColumnType.ByteType | ColumnType.ShortType | ColumnType.IntegerType => Field(
        name = cleanName,
        fieldType = IntType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, T]) => 0
      )
      case ColumnType.LongType => Field(
        name = cleanName,
        fieldType = LongType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, T]) => 0L
      )
      case ColumnType.FloatType => Field(
        name = cleanName,
        fieldType = FloatType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, T]) => 0.0
      )
      case ColumnType.ByteArrayType => Field(
        name = cleanName,
        fieldType = StringType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, T]) => s"$cleanName (ByteArray)"
      )
      case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => Field(
        name = cleanName,
        fieldType = StringType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, T]) => s"$cleanName (${col.columnType})"
      )

      case ColumnType.RefType | ColumnType.XmlType | ColumnType.UuidType => Field(
        name = cleanName,
        fieldType = StringType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, T]) => s"$cleanName (${col.columnType})"
      )

      case ColumnType.NullType | ColumnType.ObjectType | ColumnType.StructType | ColumnType.ArrayType => Field(
        name = cleanName,
        fieldType = StringType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, T]) => s"$cleanName (${col.columnType})"
      )

      case ColumnType.UnknownType => Field(
        name = cleanName,
        fieldType = StringType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, T]) => s"$cleanName (${col.columnType})"
      )
    }
  }
}
