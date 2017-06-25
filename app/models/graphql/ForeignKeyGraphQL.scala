package models.graphql

import models.result.QueryResultRow
import models.schema.ForeignKey
import sangria.schema.{Context, Field}
import sangria.schema._

object ForeignKeyGraphQL {
  def getForeignKeyField(fk: ForeignKey) = {
    Field(
      name = CommonGraphQL.cleanName(fk.name),
      fieldType = StringType,
      resolve = (x: Context[GraphQLContext, QueryResultRow]) => getStringValue(getValue(fk, x.value))
    )
  }

  private[this] def getStringValue(x: Seq[Option[String]]) = {
    x.map(_.getOrElse("--")).mkString(", ")
  }

  private[this] def getValue(fk: ForeignKey, row: QueryResultRow) = {
    fk.references.map { r =>
      row.getCell(r.source)
    }
  }
}
