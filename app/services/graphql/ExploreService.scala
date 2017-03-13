package services.graphql

import models.connection.ConnectionSettings
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
      val tableFields = fields[Unit, Table](
        Field("name", StringType, Some("The name of the table."), resolve = x => x.value.name)
      )
      ObjectType(name = table.name, description = table.description.getOrElse(s"Table [${table.name}]"), fields = tableFields)
    }

    val tableTypeFields = tableTypes.map { t =>
      Field("name", t, Some("The name of the table."), resolve = (x: Context[Unit, Table]) => x.value)
    }.toList

    //val explore = ObjectType(name = "explore", description = "Explore!", fields = tableTypeFields)

    ListType(StringType)
  }

  def resolve(user: User, cs: ConnectionSettings) = {
    SchemaService.getSchemaWithDetailsFor(user, cs).map { schema =>
      schema.tables.map(_.name)
    }
  }
}
