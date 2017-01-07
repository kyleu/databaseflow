package services.query

import java.util.UUID

import models._
import models.audit.AuditType
import models.database.Queryable
import models.queries.dynamic.DynamicQuery
import models.query.QueryResult
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.audit.AuditRecordService
import utils.{DateUtils, Logging}

import scala.concurrent.Future

object SimpleQueryService extends Logging {
  def run(db: Queryable, sql: String, user: UUID, connectionId: UUID) = {

    val startMs = DateUtils.nowMillis
    val auditId = UUID.randomUUID
    val resultId = UUID.randomUUID
    val queryId = UUID.randomUUID

    log.info(s"Performing simple query with resultId [$resultId] for query [$queryId] with sql [$sql].")
    Future(AuditRecordService.start(auditId, AuditType.Query, user, Some(connectionId), Some(sql)))

    val result = db.query(DynamicQuery(sql, Nil))

    val durationMs = (DateUtils.nowMillis - startMs).toInt
    val qr = QueryResult(queryId = queryId, sql = sql, columns = result.cols, data = result.data, rowsAffected = result.data.length, occurred = startMs)
    QueryResultResponse(resultId, 0, qr, durationMs)
  }
}
