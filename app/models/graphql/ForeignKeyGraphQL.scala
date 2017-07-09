package models.graphql

import models.query.{QueryFilter, RowDataOptions}
import models.result.QueryResultRow
import models.schema.{ColumnType, FilterOp, ForeignKey, Table}
import sangria.schema.{Context, Field, ObjectType, OptionType}
import services.query.QueryResultRowService
import utils.FutureUtils.defaultContext

object ForeignKeyGraphQL {
  def getForeignKeyField(schema: models.schema.Schema, src: Table, tgt: ObjectType[GraphQLContext, QueryResultRow], notNull: Boolean, fk: ForeignKey) = {
    val fkName = if (src.columns.exists(_.name == fk.name)) {
      "fk_" + CommonGraphQL.cleanName(fk.name)
    } else {
      CommonGraphQL.cleanName(fk.name)
    }

    def getFilters(ctx: Context[GraphQLContext, QueryResultRow]) = fk.references.map(r => QueryFilter(
      col = r.target,
      op = FilterOp.Equal,
      t = src.columns.find(_.name.equalsIgnoreCase(r.source)).map(_.columnType).getOrElse(ColumnType.StringType),
      v = ctx.value.getCell(r.source).getOrElse("")
    ))

    if (notNull) {
      Field(
        name = fkName,
        fieldType = tgt,
        description = Some(fk.name),
        resolve = (ctx: Context[GraphQLContext, QueryResultRow]) => {
          QueryResultRowService.getTableData(ctx.ctx.user, schema.connectionId, fk.targetTable, RowDataOptions(filters = getFilters(ctx))).map(_.head)
        }
      )
    } else {
      Field(
        name = fkName,
        fieldType = OptionType(tgt),
        description = Some(fk.name),
        resolve = (ctx: Context[GraphQLContext, QueryResultRow]) => {
          QueryResultRowService.getTableData(ctx.ctx.user, schema.connectionId, fk.targetTable, RowDataOptions(filters = getFilters(ctx))).map(_.headOption)
        }
      )
    }
  }
}
