package models.graphql

import java.util.UUID

import models.result.QueryResultRow
import models.schema.ColumnType
import sangria.schema._

object ColumnNotNullGraphQL {
  def getColumnField(name: String, description: Option[String], columnType: ColumnType, cleanName: String, sqlTypeName: String) = columnType match {
    case ColumnType.StringType => Field(
      name = cleanName,
      fieldType = StringType,
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getRequiredCell(name)
    )
    case ColumnType.BigDecimalType | ColumnType.DoubleType => Field(
      name = cleanName,
      fieldType = BigDecimalType,
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => BigDecimal(x.value.getRequiredCell(name))
    )
    case ColumnType.BooleanType => Field(
      name = cleanName,
      fieldType = BooleanType,
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getRequiredCell(name) == "true"
    )
    case ColumnType.ByteType | ColumnType.ShortType | ColumnType.IntegerType => Field(
      name = cleanName,
      fieldType = IntType,
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getRequiredCell(name).toInt
    )
    case ColumnType.LongType => Field(
      name = cleanName,
      fieldType = LongType,
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getRequiredCell(name).toLong
    )
    case ColumnType.FloatType => Field(
      name = cleanName,
      fieldType = FloatType,
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getRequiredCell(name).toDouble
    )
    case ColumnType.ByteArrayType => Field(
      name = cleanName,
      fieldType = StringType,
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getRequiredCell(name)
    )
    case ColumnType.DateType | ColumnType.TimeType | ColumnType.TimestampType => Field(
      name = cleanName,
      fieldType = StringType,
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getRequiredCell(name)
    )
    case ColumnType.UuidType => Field(
      name = cleanName,
      fieldType = CommonGraphQL.uuidType,
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => UUID.fromString(x.value.getRequiredCell(name))
    )
    case ColumnType.ArrayType => ArrayGraphQL.getArrayField(name, description, cleanName, sqlTypeName)
    case ColumnType.RefType | ColumnType.XmlType => Field(
      name = cleanName,
      fieldType = StringType,
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getRequiredCell(name)
    )
    case ColumnType.ObjectType | ColumnType.StructType => Field(
      name = cleanName,
      fieldType = StringType,
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getRequiredCell(name)
    )
    case ColumnType.UnknownType => Field(
      name = cleanName,
      fieldType = StringType,
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getRequiredCell(name)
    )
  }
}
