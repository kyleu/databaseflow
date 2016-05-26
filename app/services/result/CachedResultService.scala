package services.result

import java.util.UUID

import models.ddl.DdlQueries
import models.queries.result.CachedResultQueries
import models.result.{ CachedResult, CachedResultQuery }
import services.database.{ MasterDatabase, ResultCacheDatabase }
import services.schema.MetadataTables
import utils.Logging

import scala.util.control.NonFatal

object CachedResultService extends Logging {
  def insertCacheResult(resultId: UUID, queryId: UUID, connectionId: UUID, owner: Option[UUID], sql: String) = {
    val model = CachedResult(resultId, queryId, connectionId, owner, "starting", sql)
    log.info(s"Caching result [$model].")
    MasterDatabase.conn.executeUpdate(CachedResultQueries.insert(model))
    model
  }

  def getAll = MasterDatabase.conn.query(CachedResultQueries.getAll(orderBy = "\"created\" desc"))

  def remove(resultId: UUID) = {
    MasterDatabase.conn.executeUpdate(CachedResultQueries.removeById(resultId))
    try {
      ResultCacheDatabase.conn.executeUpdate(DdlQueries.DropTable("result_" + resultId.toString.replaceAllLiterally("-", ""))(ResultCacheDatabase.conn.engine))
    } catch {
      case NonFatal(x) => // no op
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
