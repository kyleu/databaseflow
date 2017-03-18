package models.graphql

import models.result.QueryResultSet
import models.schema.{Column, ColumnType}
import sangria.schema._

object ColumnNotNullGraphQL {
  def getColumnField(col: Column) = {
    if (!col.notNull) {
      throw new IllegalStateException(s"Nullable column [${col.name}] passed to non-nullable method.")
    }
    val cleanName = col.name.replaceAllLiterally(" ", "")
    col.columnType match {
      case ColumnType.StringType => Field(
        name = cleanName,
        fieldType = StringType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultSet]) => x.value.getRequiredCell(col.name)
      )
      case ColumnType.BigDecimalType | ColumnType.DoubleType => Field(
        name = cleanName,
        fieldType = BigDecimalType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultSet]) => BigDecimal(x.value.getRequiredCell(col.name))
      )
      case ColumnType.BooleanType => Field(
        name = cleanName,
        fieldType = BooleanType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultSet]) => x.value.getRequiredCell(col.name) == "true"
      )
      case ColumnType.ByteType | ColumnType.ShortType | ColumnType.IntegerType => Field(
        name = cleanName,
        fieldType = IntType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultSet]) => x.value.getRequiredCell(col.name).toInt
      )
      case ColumnType.LongType => Field(
        name = cleanName,
        fieldType = LongType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultSet]) => x.value.getRequiredCell(col.name).toLong
      )
      case ColumnType.FloatType => Field(
        name = cleanName,
        fieldType = FloatType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultSet]) => x.value.getRequiredCell(col.name).toDouble
      )
      case ColumnType.ByteArrayType => Field(
        name = cleanName,
        fieldType = StringType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultSet]) => s"${x.value.getRequiredCell(col.name)} (${col.columnType})"
      )
      case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => Field(
        name = cleanName,
        fieldType = StringType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultSet]) => s"${x.value.getRequiredCell(col.name)} (${col.columnType})"
      )

      case ColumnType.RefType | ColumnType.XmlType | ColumnType.UuidType => Field(
        name = cleanName,
        fieldType = StringType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultSet]) => s"${x.value.getRequiredCell(col.name)} (${col.columnType})"
      )

      case ColumnType.NullType | ColumnType.ObjectType | ColumnType.StructType | ColumnType.ArrayType => Field(
        name = cleanName,
        fieldType = StringType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultSet]) => s"${x.value.getRequiredCell(col.name)} (${col.columnType})"
      )

      case ColumnType.UnknownType => Field(
        name = cleanName,
        fieldType = StringType,
        description = col.description,
        resolve = (x: Context[GraphQLContext, QueryResultSet]) => s"${x.value.getRequiredCell(col.name)} (${col.columnType})"
      )
    }
  }
}
