package services.explore

import models.graphql.{ColumnGraphQL, GraphQLContext}
import models.result.QueryResultGraphQL._
import models.result.QueryResultRow
import models.schema.SchemaModelGraphQL
import sangria.schema._
import services.query.QueryResultRowService

object ExploreTableHelper {
  def getTables(schema: models.schema.Schema) = if (schema.tables.isEmpty) {
    None
  } else {
    val tableTypes = schema.tables.map { table =>
      val tableFieldset = fields[GraphQLContext, QueryResultRow](table.columns.map(col => ColumnGraphQL.getColumnField(col)): _*)
      table -> ObjectType(name = table.name, description = table.description.getOrElse(s"Table [${table.name}]"), fields = tableFieldset)
    }

    val tableFields = fields[GraphQLContext, Unit](tableTypes.map { t =>
      Field(
        name = t._1.name,
        fieldType = ListType(t._2),
        description = t._1.description,
        //resolve = (x: Context[GraphQLContext, Unit]) => QueryResultRow.mock(t._1.columns, x.arg(limitArg).getOrElse(100)),
        resolve = (x: Context[GraphQLContext, Unit]) => {
          QueryResultRowService.getTableData(x.ctx.user, schema.connectionId, t._1.name, SchemaModelGraphQL.rowDataOptionsFor(x))
        },
        arguments = resultArgs
      )
    }: _*)

    Some(ObjectType(name = "tables", description = "The tables contained in this schema.", fields = tableFields))
  }
}
