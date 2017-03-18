package services.result

import java.util.UUID

import models.user.User
import services.database.DatabaseRegistry

object QueryResultService {
  def forTable(user: User, connectionId: UUID, name: String) = {
    val db = DatabaseRegistry.db(user, connectionId)

  }
}
