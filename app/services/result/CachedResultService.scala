package services.result

import java.util.UUID

import models.ddl.DdlQueries
import models.queries.result.CachedResultQueries
import models.result.CachedResult
import services.database.{MasterDatabase, ResultCacheDatabase}
import services.schema.MetadataTables
import utils.Logging
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.util.control.NonFatal

object CachedResultService extends Logging {
  def insertCacheResult(result: CachedResult) = {
    log.info(s"Caching result [$result].")
    MasterDatabase.conn.query(CachedResultQueries.findBy(result.queryId, result.owner)).foreach { existing =>
      Future(remove(existing.resultId))
    }
    MasterDatabase.conn.executeUpdate(CachedResultQueries.insert(result))
    result
  }

  def setFirstMessageDuration(resultId: UUID, firstMessageDuration: Int) = {
    MasterDatabase.conn.executeUpdate(CachedResultQueries.SetFirstMessageDuration(resultId, firstMessageDuration))
  }

  def completeCacheResult(resultId: UUID, rowCount: Int, duration: Int) = {
    MasterDatabase.conn.executeUpdate(CachedResultQueries.Complete(resultId, rowCount, duration))
  }

  def getAll = MasterDatabase.conn.query(CachedResultQueries.getAll(orderBy = "\"created\" desc"))

  def remove(resultId: UUID) = {
    MasterDatabase.conn.executeUpdate(CachedResultQueries.removeById(resultId))
    try {
      ResultCacheDatabase.conn.executeUpdate(DdlQueries.DropTable("result_" + resultId.toString.replaceAllLiterally("-", ""))(ResultCacheDatabase.conn.engine))
    } catch {
      case NonFatal(x) => log.info(s"Encountered exception while dropping table for [${resultId}].", x)
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
}
