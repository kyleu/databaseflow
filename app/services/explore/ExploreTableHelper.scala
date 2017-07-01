package services.explore

import models.graphql.{ColumnGraphQL, CommonGraphQL, ForeignKeyGraphQL, GraphQLContext}
import models.result.QueryResultGraphQL._
import models.result.QueryResultRow
import models.schema.{SchemaModelGraphQL, Table}
import sangria.schema._
import services.query.QueryResultRowService

object ExploreTableHelper {
  def getTables(schema: models.schema.Schema) = if (schema.tables.isEmpty) {
    None
  } else {
    val tableTypes = collection.mutable.ArrayBuffer.empty[(Table, ObjectType[GraphQLContext, QueryResultRow])]

    def tableFieldset(table: Table) = {
      val columnFields = table.columns.map { col =>
        ColumnGraphQL.getColumnField(CommonGraphQL.cleanName(col.name), col.name, col.description, col.columnType, col.notNull)
      }
      val fkFields = table.foreignKeys.map(fk => ForeignKeyGraphQL.getForeignKeyField(schema, tableTypes, table, fk))
      fields[GraphQLContext, QueryResultRow](columnFields ++ fkFields: _*)
    }

    schema.tables.map { table =>
      val fn = () => tableFieldset(table)
      tableTypes += table -> ObjectType(name = table.name, description = table.description.getOrElse(s"Table [${table.name}]"), fieldsFn = fn)
    }

    val tableFields = fields[GraphQLContext, Unit](tableTypes.map { t =>
      Field(
        name = t._1.name,
        fieldType = ListType(t._2),
        description = t._1.description,
        resolve = (x: Context[GraphQLContext, Unit]) => {
          QueryResultRowService.getTableData(x.ctx.user, schema.connectionId, t._1.name, SchemaModelGraphQL.rowDataOptionsFor(x))
        },
        arguments = resultArgs
      )
    }: _*)

    Some(ObjectType(name = "tables", description = "The tables contained in this schema.", fields = tableFields))
  }
}
