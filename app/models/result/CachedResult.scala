package models.result

import java.util.UUID

import org.joda.time.LocalDateTime
import utils.DateUtils

case class CachedResult(
    resultId: UUID,
    queryId: UUID,
    connectionId: UUID,
    owner: UUID,
    status: String = "starting",
    source: Option[String],
    sql: String,
    columns: Int = 0,
    rows: Int = 0,
    firstMessage: Int = 0,
    duration: Int = 0,
    lastAccessed: LocalDateTime = DateUtils.now,
    created: LocalDateTime = DateUtils.now
) {
  lazy val tableName = "result_" + resultId.toString.replaceAllLiterally("-", "")
}
