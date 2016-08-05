package models.settings

import models.connection.ConnectionSettings
import models.query.SavedQuery

case class ExportModel(settings: Seq[Setting], connections: Seq[ConnectionSettings], savedQueries: Seq[SavedQuery])
