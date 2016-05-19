package services.result

import java.util.UUID

import models.queries.result.{ CachedResultQueries, CreateResultTable }
import models.result.CachedResult
import services.database.MasterDatabase
import utils.DateUtils

object CachedResultService {
  def cache(resultId: UUID, queryId: UUID, connectionId: UUID, owner: Option[UUID], sql: String) = {
    val status = "starting"
    val columns = 0
    val rows = 0
    val duration = 0
    val lastAccessed = DateUtils.now
    val created = DateUtils.now

    val model = CachedResult(resultId, queryId, connectionId, owner, status, sql, columns, rows, duration, lastAccessed, created)
    MasterDatabase.conn.executeUpdate(CachedResultQueries.insert(model))

    MasterDatabase.conn.executeUpdate(CreateResultTable(resultId, Nil)(MasterDatabase.conn.engine))
  }

  def getAll = MasterDatabase.conn.query(CachedResultQueries.getAll(orderBy = "created desc"))
}
