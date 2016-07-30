package models.settings

import models.connection.ConnectionSettings
import models.query.SavedQuery
import models.user.User

case class ExportModel(settings: Seq[Setting], users: Seq[User], connections: Seq[ConnectionSettings], savedQueries: Seq[SavedQuery])
