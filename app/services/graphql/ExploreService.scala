package services.graphql

import models.connection.ConnectionSettings
import models.graphql.{ColumnGraphQL, GraphQLContext}
import models.schema.{Table, View}
import models.user.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import sangria.schema._
import services.schema.SchemaService
import models.result.QueryResultGraphQL._

import scala.concurrent.Await

object ExploreService {
  def exploreType(cs: ConnectionSettings) = {
    import scala.concurrent.duration._
    val f = SchemaService.getSchemaWithDetails(cs)
    val schema = Await.result(f, 60.seconds)

    val tableTypes = schema.tables.map { table =>
      val tableFieldset = fields[GraphQLContext, Table](table.columns.map(col => ColumnGraphQL.getColumnField[Table](col)): _*)
      ObjectType(name = table.name, description = table.description.getOrElse(s"Table [${table.name}]"), fields = tableFieldset)
    }

    val tableFields = fields[GraphQLContext, Table](tableTypes.map { t =>
      Field(
        name = t.name,
        fieldType = ListType(t),
        description = t.description,
        resolve = (x: Context[GraphQLContext, Table]) => Seq(x.value),
        arguments = resultArgs
      )
    }: _*)

    val tables = ObjectType(name = "tables", description = "The tables contained in this schema.", fields = tableFields)

    val viewTypes = schema.views.map { view =>
      val fieldset = fields[GraphQLContext, View](view.columns.map(col => ColumnGraphQL.getColumnField[View](col)): _*)
      ObjectType(name = view.name, description = view.description.getOrElse(s"View [${view.name}]"), fields = fieldset)
    }

    val viewFields = fields[GraphQLContext, View](viewTypes.map { v =>
      Field(
        name = v.name,
        fieldType = ListType(v),
        description = v.description,
        resolve = (x: Context[GraphQLContext, View]) => Seq(x.value),
        arguments = resultArgs
      )
    }: _*)

    val views = ObjectType(name = "views", description = "The views contained in this schema.", fields = viewFields)

    val exploreFields = tableFields

    val explore = ObjectType(name = "explore", description = "Explore!", fields = exploreFields)

    explore
  }

  def resolve(user: User, cs: ConnectionSettings) = {
    SchemaService.getSchemaWithDetailsFor(user, cs).map { schema =>
      schema.tables.head
    }
  }
}
