package services.result

import java.util.UUID

import models.ddl.DdlQueries
import models.queries.result.CachedResultQueries
import models.result.CachedResult
import org.joda.time.LocalDateTime
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.database.core.{MasterDatabase, ResultCacheDatabase}
import services.query.SharedResultService
import services.schema.MetadataTables
import utils.Logging

import scala.concurrent.Future
import scala.util.control.NonFatal

object CachedResultService extends Logging {
  case class CleanupResult(orphans: Seq[String], removed: Seq[UUID], remainingCount: Int) {
    override def toString = {
      s"Query result cleanup removed [${removed.size}] results, along with [${orphans.size}] orphans, leaving [$remainingCount] remaining."
    }
  }

  def insertCacheResult(result: CachedResult) = {
    log.info(s"Caching result [$result].")
    MasterDatabase.executeUpdate(CachedResultQueries.insert(result))
    result
  }

  def removeCacheResults(userId: UUID, queryId: UUID) = {
    val f = Future {
      MasterDatabase.query(CachedResultQueries.GetMatchingResultIds(userId, queryId)).foreach(remove)
    }
    f.onFailure {
      case x => log.error("Cannot remove cache results.", x)
    }
  }

  def setFirstMessageDuration(resultId: UUID, firstMessageDuration: Int) = {
    MasterDatabase.executeUpdate(CachedResultQueries.SetFirstMessageDuration(resultId, firstMessageDuration))
  }

  def completeCacheResult(resultId: UUID, rowCount: Int, duration: Int) = {
    MasterDatabase.executeUpdate(CachedResultQueries.Complete(resultId, rowCount, duration))
  }

  def getAll = MasterDatabase.query(CachedResultQueries.getAll(orderBy = "\"created\" desc"))

  def remove(resultId: UUID): Unit = if (SharedResultService.containsResultId(resultId)) {
    log.warn("Cannot remove shared results.")
  } else {
    MasterDatabase.executeUpdate(CachedResultQueries.removeById(resultId))
    try {
      ResultCacheDatabase.conn.executeUpdate(DdlQueries.DropTable("result_" + resultId.toString.replaceAllLiterally("-", ""))(ResultCacheDatabase.conn.engine))
    } catch {
      case NonFatal(x) => log.info(s"Encountered exception while dropping table for [$resultId].", x)
    }
  }

  def getTables = ResultCacheDatabase.conn.withConnection { conn =>
    val metadata = conn.getMetaData
    val schema = try {
      Option(conn.getSchema)
    } catch {
      case _: AbstractMethodError => None
    }
    val ret = MetadataTables.getTables(metadata, Option(conn.getCatalog), schema)
    ret.map(_.name).filter(_.startsWith("result_")).toSet
  }

  def cleanup(before: LocalDateTime) = {
    val sharedIds = SharedResultService.getSharedCachedResultIds
    val rows = CachedResultService.getAll
    val tables = CachedResultService.getTables
    val tableNames = rows.map(_.tableName).toSet
    val orphans = tables.toSeq.filterNot(tableNames.contains).sorted
    orphans.foreach { orphan =>
      ResultCacheDatabase.conn.executeUpdate(DdlQueries.DropTable(orphan)(ResultCacheDatabase.conn.engine))
    }

    val removed = rows.flatMap { row =>
      if (row.created.isBefore(before) && (!sharedIds.contains(row.resultId))) {
        remove(row.resultId)
        Some(row.resultId)
      } else {
        None
      }
    }

    val remaining = rows.size - removed.size
    CleanupResult(orphans, removed, remaining)
  }
}
