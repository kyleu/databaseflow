package services.graphql

import models.connection.ConnectionSettings
import models.user.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.schema.SchemaService

object ExploreService {
  def resolve(user: User, cs: ConnectionSettings) = {
    SchemaService.getSchemaWithDetailsFor(user, cs).map { schema =>
      schema.tables.map(_.name)
    }
  }
}
