package services.result

import java.util.UUID

import models.ddl.DdlQueries
import models.queries.result.{ CachedResultQueries }
import models.result.{ CachedResult, CachedResultQuery }
import services.database.{ MasterDatabase, ResultCacheDatabase }
import services.schema.MetadataTables

object CachedResultService {
  def cache(resultId: UUID, queryId: UUID, connectionId: UUID, owner: Option[UUID], sql: String) = {
    val status = "starting"

    val model = CachedResult(resultId, queryId, connectionId, owner, status, sql)
    MasterDatabase.conn.executeUpdate(CachedResultQueries.insert(model))

    val ret = MasterDatabase.databaseFor(connectionId) match {
      case Right(db) => db.executeUnknown(CachedResultQuery(model))
      case Left(t) => throw t
    }

    ret
  }

  def getAll = MasterDatabase.conn.query(CachedResultQueries.getAll(orderBy = "\"created\" desc"))

  def remove(resultId: UUID) = {
    MasterDatabase.conn.executeUpdate(CachedResultQueries.removeById(resultId))
    ResultCacheDatabase.conn.executeUpdate(DdlQueries.DropTable("result_" + resultId.toString.replaceAllLiterally("-", ""))(ResultCacheDatabase.conn.engine))
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
