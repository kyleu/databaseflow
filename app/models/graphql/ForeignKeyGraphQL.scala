package models.graphql

import models.result.QueryResultRow
import models.schema.{ForeignKey, Table}
import sangria.schema.{Context, Field, ObjectType, ListType}
import services.query.QueryResultRowService

object ForeignKeyGraphQL {
  def getForeignKeyField(schema: models.schema.Schema, tableTypes: Seq[(Table, ObjectType[GraphQLContext, QueryResultRow])], table: Table, fk: ForeignKey) = {
    val tableType = tableTypes.find(_._1.name.equalsIgnoreCase(fk.targetTable)).map(_._2).getOrElse {
      throw new IllegalStateException(s"Missing output type for [${table.name}].")
    }
    val whereClause = fk.references.map(r => s"${r.target} = ?").mkString(" and ")

    Field(
      name = CommonGraphQL.cleanName(fk.name),
      fieldType = ListType(tableType),
      description = Some(fk.name),
      resolve = (ctx: Context[GraphQLContext, QueryResultRow]) => {
        val values = fk.references.map(r => ctx.value.getCell(r.source))
        QueryResultRowService.getTableDataWhereClause(ctx.ctx.user, schema.connectionId, table.name, whereClause, values)
      }
    )
  }
}
