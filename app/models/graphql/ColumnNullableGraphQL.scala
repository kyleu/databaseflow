package models.graphql

import java.util.UUID

import models.result.QueryResultRow
import models.schema.ColumnType
import sangria.schema._

object ColumnNullableGraphQL {
  def getColumnField(name: String, description: Option[String], columnType: ColumnType, cleanName: String, sqlTypeName: String) = columnType match {
    case ColumnType.StringType => Field(
      name = cleanName,
      fieldType = OptionType(StringType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name)
    )
    case ColumnType.BigDecimalType | ColumnType.DoubleType => Field(
      name = cleanName,
      fieldType = OptionType(BigDecimalType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name).map(BigDecimal(_))
    )
    case ColumnType.BooleanType => Field(
      name = cleanName,
      fieldType = OptionType(BooleanType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name).map(_ == "true")
    )
    case ColumnType.ByteType | ColumnType.ShortType | ColumnType.IntegerType => Field(
      name = cleanName,
      fieldType = OptionType(IntType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name).map(_.toInt)
    )
    case ColumnType.LongType => Field(
      name = cleanName,
      fieldType = OptionType(LongType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name).map(_.toLong)
    )
    case ColumnType.FloatType => Field(
      name = cleanName,
      fieldType = OptionType(FloatType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name).map(_.toDouble)
    )
    case ColumnType.ByteArrayType => Field(
      name = cleanName,
      fieldType = OptionType(StringType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name)
    )
    case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => Field(
      name = cleanName,
      fieldType = OptionType(StringType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name)
    )
    case ColumnType.UuidType => Field(
      name = cleanName,
      fieldType = OptionType(CommonGraphQL.uuidType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name).map(UUID.fromString)
    )
    case ColumnType.ArrayType => ArrayGraphQL.getOptArrayField(name, description, cleanName, sqlTypeName)
    case ColumnType.RefType | ColumnType.XmlType => Field(
      name = cleanName,
      fieldType = OptionType(StringType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name)
    )
    case ColumnType.ObjectType | ColumnType.StructType => Field(
      name = cleanName,
      fieldType = OptionType(StringType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name)
    )
    case ColumnType.UnknownType => Field(
      name = cleanName,
      fieldType = OptionType(StringType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name)
    )
  }
}
