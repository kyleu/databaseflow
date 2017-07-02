package models.graphql

import models.query.{QueryFilter, RowDataOptions}
import models.result.QueryResultRow
import models.schema.{ColumnType, FilterOp, ForeignKey, Table}
import sangria.schema.{Context, Field, ListType, ObjectType}
import services.query.QueryResultRowService

object ForeignKeyGraphQL {
  def getForeignKeyField(schema: models.schema.Schema, tableTypes: Seq[(Table, ObjectType[GraphQLContext, QueryResultRow])], table: Table, fk: ForeignKey) = {
    val tableType = tableTypes.find(_._1.name.equalsIgnoreCase(fk.targetTable)).map(_._2).getOrElse {
      throw new IllegalStateException(s"Missing output type for [${table.name}].")
    }

    Field(
      name = CommonGraphQL.cleanName(fk.name),
      fieldType = ListType(tableType),
      description = Some(fk.name),
      resolve = (ctx: Context[GraphQLContext, QueryResultRow]) => {
        val filters = fk.references.map(r => QueryFilter(
          col = r.target,
          op = FilterOp.Equal,
          t = table.columns.find(_.name.equalsIgnoreCase(r.source)).map(_.columnType).getOrElse(ColumnType.StringType),
          v = ctx.value.getCell(r.source).getOrElse("")
        ))
        val options = RowDataOptions(filters = filters)
        QueryResultRowService.getTableData(ctx.ctx.user, schema.connectionId, fk.targetTable, options)
      }
    )
  }
}
