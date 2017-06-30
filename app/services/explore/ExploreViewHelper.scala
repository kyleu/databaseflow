package services.explore

import models.graphql.{ColumnGraphQL, CommonGraphQL, GraphQLContext}
import models.result.QueryResultGraphQL._
import models.result.QueryResultRow
import models.schema.SchemaModelGraphQL
import sangria.schema._
import services.query.QueryResultRowService

object ExploreViewHelper {
  def getViews(schema: models.schema.Schema) = if (schema.views.isEmpty) {
    None
  } else {
    val viewTypes = schema.views.map { view =>
      val fieldset = fields[GraphQLContext, QueryResultRow](view.columns.map { col =>
        ColumnGraphQL.getColumnField(CommonGraphQL.cleanName(col.name), col.name, col.description, col.columnType, col.notNull)
      }: _*)
      view -> ObjectType(name = view.name, description = view.description.getOrElse(s"View [${view.name}]"), fields = fieldset)
    }

    val viewFields = fields[GraphQLContext, Unit](viewTypes.map { v =>
      Field(
        name = v._1.name,
        fieldType = ListType(v._2),
        description = v._1.description,
        resolve = (x: Context[GraphQLContext, Unit]) => {
          QueryResultRowService.getViewData(x.ctx.user, schema.connectionId, v._1.name, SchemaModelGraphQL.rowDataOptionsFor(x))
        },
        arguments = resultArgs
      )
    }: _*)

    Some(ObjectType(name = "views", description = "The views contained in this schema.", fields = viewFields))
  }
}
