package models.graphql

import java.util.UUID

import models.result.QueryResultRow
import sangria.schema._

object ArrayGraphQL {
  private[this] def clean(s: String) = s.stripPrefix("{").stripSuffix("}").split(',').map(_.trim).toSeq

  def getArrayField(name: String, description: Option[String], cleanName: String, sqlTypeName: String) = sqlTypeName match {
    case x if x.startsWith("_int") => Field(
      name = cleanName,
      fieldType = ListType(LongType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => clean(x.value.getRequiredCell(name)).map(_.toLong)
    )
    case x if x.startsWith("_uuid") => Field(
      name = cleanName,
      fieldType = ListType(CommonGraphQL.uuidType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => clean(x.value.getRequiredCell(name)).map(UUID.fromString)
    )
    case x if x.startsWith("_varchar") => Field(
      name = cleanName,
      fieldType = ListType(StringType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => clean(x.value.getRequiredCell(name))
    )
    case _ => Field(
      name = cleanName,
      fieldType = ListType(StringType),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => clean(x.value.getRequiredCell(name))
    )
  }

  def getOptArrayField(name: String, description: Option[String], cleanName: String, sqlTypeName: String) = sqlTypeName match {
    case x if x.startsWith("_int") => Field(
      name = cleanName,
      fieldType = OptionType(ListType(LongType)),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name).map(clean).map(_.map(_.toLong))
    )
    case x if x.startsWith("_uuid") => Field(
      name = cleanName,
      fieldType = OptionType(ListType(CommonGraphQL.uuidType)),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name).map(clean).map(_.map(UUID.fromString))
    )
    case x if x.startsWith("_varchar") => Field(
      name = cleanName,
      fieldType = OptionType(ListType(StringType)),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name).map(clean)
    )
    case _ => Field(
      name = cleanName,
      fieldType = OptionType(ListType(StringType)),
      description = description,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => x.value.getCell(name).map(clean)
    )
  }
}
