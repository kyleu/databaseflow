package services.graphql

import models.connection.ConnectionSettings
import models.graphql.{ColumnGraphQL, GraphQLContext, Resultset}
import models.schema.{Table, View}
import models.user.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import sangria.schema._
import services.schema.SchemaService
import models.result.QueryResultGraphQL._

import scala.concurrent.Await

object ExploreService {
  private[this] def getTables(schema: models.schema.Schema) = {
    val tableTypes = schema.tables.map { table =>
      val tableFieldset = fields[GraphQLContext, Resultset](table.columns.map(col => ColumnGraphQL.getColumnField(col)): _*)
      table -> ObjectType(name = table.name, description = table.description.getOrElse(s"Table [${table.name}]"), fields = tableFieldset)
    }

    val tableFields = fields[GraphQLContext, Table](tableTypes.map { t =>
      Field(
        name = t._1.name,
        fieldType = ListType(t._2),
        description = t._1.description,
        resolve = (x: Context[GraphQLContext, Table]) => Resultset.mock(t._1.columns, x.arg(limitArg).getOrElse(100)),
        arguments = resultArgs
      )
    }: _*)

    ObjectType(name = "tables", description = "The tables contained in this schema.", fields = tableFields)
  }

  private[this] def getViews(schema: models.schema.Schema) = {
    val viewTypes = schema.views.map { view =>
      val fieldset = fields[GraphQLContext, Resultset](view.columns.map(col => ColumnGraphQL.getColumnField(col)): _*)
      ObjectType(name = view.name, description = view.description.getOrElse(s"View [${view.name}]"), fields = fieldset)
    }

    val viewFields = fields[GraphQLContext, View](viewTypes.map { v =>
      Field(
        name = v.name,
        fieldType = ListType(v),
        description = v.description,
        resolve = (x: Context[GraphQLContext, View]) => Resultset.mock(x.value.columns, x.arg(limitArg).getOrElse(100)),
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

    val explore = "???"

    tables
  }

  def resolve(user: User, cs: ConnectionSettings) = {
    SchemaService.getSchemaWithDetailsFor(user, cs).map { schema =>
      schema.tables.head
    }
  }
}
