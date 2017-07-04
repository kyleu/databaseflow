package models.graphql

import models.query.{QueryFilter, RowDataOptions}
import models.result.QueryResultRow
import models.schema.{ColumnType, FilterOp, ForeignKey, Table}
import sangria.schema.{Context, Field, ListType, ObjectType}
import services.query.QueryResultRowService

object ForeignKeyGraphQL {
  def getForeignKeyField(schema: models.schema.Schema, src: Table, tgt: ObjectType[GraphQLContext, QueryResultRow], fk: ForeignKey) = {
    Field(
      name = "fk_" + CommonGraphQL.cleanName(fk.name),
      fieldType = ListType(tgt),
      description = Some(fk.name),
      resolve = (ctx: Context[GraphQLContext, QueryResultRow]) => {
        val filters = fk.references.map(r => QueryFilter(
          col = r.target,
          op = FilterOp.Equal,
          t = src.columns.find(_.name.equalsIgnoreCase(r.source)).map(_.columnType).getOrElse(ColumnType.StringType),
          v = ctx.value.getCell(r.source).getOrElse("")
        ))
        QueryResultRowService.getTableData(ctx.ctx.user, schema.connectionId, fk.targetTable, RowDataOptions(filters = filters))
      }
    )
  }
}
