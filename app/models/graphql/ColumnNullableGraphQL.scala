package models.graphql

import models.result.QueryResultRow
import models.schema.{Column, ColumnType}
import sangria.schema._

object ColumnNullableGraphQL {
  def getColumnField(col: Column, cleanName: String) = {
    if (col.notNull) {
      throw new IllegalStateException(s"Not-null column [${col.name}] passed to nullable method.")
    }
    col.columnType match {
      case ColumnType.StringType => Field(
        name = cleanName,
        fieldType = OptionType(StringType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(col.name)
      )
      case ColumnType.BigDecimalType | ColumnType.DoubleType => Field(
        name = cleanName,
        fieldType = OptionType(BigDecimalType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(col.name).map(BigDecimal(_))
      )
      case ColumnType.BooleanType => Field(
        name = cleanName,
        fieldType = OptionType(BooleanType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(col.name).map(_ == "true")
      )
      case ColumnType.ByteType | ColumnType.ShortType | ColumnType.IntegerType => Field(
        name = cleanName,
        fieldType = OptionType(IntType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(col.name).map(_.toInt)
      )
      case ColumnType.LongType => Field(
        name = cleanName,
        fieldType = OptionType(LongType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(col.name).map(_.toLong)
      )
      case ColumnType.FloatType => Field(
        name = cleanName,
        fieldType = OptionType(FloatType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(col.name).map(_.toDouble)
      )
      case ColumnType.ByteArrayType => Field(
        name = cleanName,
        fieldType = OptionType(StringType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(col.name)
      )
      case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => Field(
        name = cleanName,
        fieldType = OptionType(StringType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(col.name)
      )

      case ColumnType.RefType | ColumnType.XmlType | ColumnType.UuidType => Field(
        name = cleanName,
        fieldType = OptionType(StringType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(col.name)
      )

      case ColumnType.NullType | ColumnType.ObjectType | ColumnType.StructType | ColumnType.ArrayType => Field(
        name = cleanName,
        fieldType = OptionType(StringType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(col.name)
      )

      case ColumnType.UnknownType => Field(
        name = cleanName,
        fieldType = OptionType(StringType),
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(col.name)
      )
    }
  }
}
