package services.graphql

import models.connection.ConnectionSettings
import models.graphql.GraphQLContext
import models.schema.Table
import models.user.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import sangria.schema._
import services.schema.SchemaService

import scala.concurrent.Await

object ExploreService {
  def exploreType(cs: ConnectionSettings) = {
    import scala.concurrent.duration._
    val f = SchemaService.getSchemaWithDetails(cs)
    val schema = Await.result(f, 60.seconds)

    val tableTypes = schema.tables.map { table =>
      val tableFields = fields[GraphQLContext, Table](table.columns.map { col =>
        Field(col.name, StringType, col.description, resolve = (x: Context[GraphQLContext, Table]) => col.name + " (TODO)")
      }: _*)
      ObjectType(name = table.name, description = table.description.getOrElse(s"Table [${table.name}]"), fields = tableFields)
    }

    val exploreFields = fields[GraphQLContext, Table](tableTypes.map { t =>
      Field(t.name, t, t.description, resolve = (x: Context[GraphQLContext, Table]) => x.value)
    }: _*)

    val explore = ObjectType(name = "explore", description = "Explore!", fields = exploreFields)

    explore
  }

  def resolve(user: User, cs: ConnectionSettings) = {
    SchemaService.getSchemaWithDetailsFor(user, cs).map { schema =>
      schema.tables.head
    }
  }
}
