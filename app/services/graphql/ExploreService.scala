package services.graphql

import models.connection.ConnectionSettings
import models.graphql.{ColumnGraphQL, GraphQLContext}
import models.user.User
import sangria.schema._
import services.schema.SchemaService
import models.result.QueryResultGraphQL._
import models.result.QueryResultSet

import scala.concurrent.Await

object ExploreService {
  private[this] def getTables(schema: models.schema.Schema) = {
    val tableTypes = schema.tables.map { table =>
      val tableFieldset = fields[GraphQLContext, QueryResultSet](table.columns.map(col => ColumnGraphQL.getColumnField(col)): _*)
      table -> ObjectType(name = table.name, description = table.description.getOrElse(s"Table [${table.name}]"), fields = tableFieldset)
    }

    val tableFields = fields[GraphQLContext, Unit](tableTypes.map { t =>
      Field(
        name = t._1.name,
        fieldType = ListType(t._2),
        description = t._1.description,
        resolve = (x: Context[GraphQLContext, Unit]) => QueryResultSet.mock(t._1.columns, x.arg(limitArg).getOrElse(100)),
        arguments = resultArgs
      )
    }: _*)

    ObjectType(name = "tables", description = "The tables contained in this schema.", fields = tableFields)
  }

  private[this] def getViews(schema: models.schema.Schema) = {
    val viewTypes = schema.views.map { view =>
      val fieldset = fields[GraphQLContext, QueryResultSet](view.columns.map(col => ColumnGraphQL.getColumnField(col)): _*)
      view -> ObjectType(name = view.name, description = view.description.getOrElse(s"View [${view.name}]"), fields = fieldset)
    }

    val viewFields = fields[GraphQLContext, Unit](viewTypes.map { v =>
      Field(
        name = v._1.name,
        fieldType = ListType(v._2),
        description = v._1.description,
        resolve = (x: Context[GraphQLContext, Unit]) => QueryResultSet.mock(v._1.columns, x.arg(limitArg).getOrElse(100)),
        arguments = resultArgs
      )
    }: _*)

    ObjectType(name = "views", description = "The views contained in this schema.", fields = viewFields)
  }

  def exploreType(cs: ConnectionSettings) = {
    import scala.concurrent.duration._
    val f = SchemaService.getSchemaWithDetails(cs)
    val schema = Await.result(f, 60.seconds)

    val tables = getTables(schema)
    val views = getViews(schema)

    val exploreFields = fields[GraphQLContext, models.schema.Schema](
      Field(
        name = "table",
        fieldType = tables,
        description = Some("This database's tables."),
        resolve = (x: Context[GraphQLContext, models.schema.Schema]) => ()
      ),
      Field(
        name = "view",
        fieldType = views,
        description = Some("This database's views."),
        resolve = (x: Context[GraphQLContext, models.schema.Schema]) => ()
      )
    )

    val explore = ObjectType(name = "explore", description = "Explore this database's objects in a simple graph.", fields = exploreFields)

    explore
  }

  def resolve(user: User, cs: ConnectionSettings) = {
    SchemaService.getSchemaWithDetailsFor(user, cs)
  }
}
