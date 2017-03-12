package services.graphql

import models.connection.ConnectionSettings
import models.user.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import sangria.schema.{ListType, StringType}
import services.schema.SchemaService

import scala.concurrent.Await

object ExploreService {
  def exploreType(cs: ConnectionSettings) = {
    import scala.concurrent.duration._
    val f = SchemaService.getSchemaWithDetails(cs).map { schema =>
      schema.tables.map(_.name)
      ListType(StringType)
    }
    Await.result(f, 60.seconds)
  }

  def resolve(user: User, cs: ConnectionSettings) = {
    SchemaService.getSchemaWithDetailsFor(user, cs).map { schema =>
      schema.tables.map(_.name)
    }
  }
}
