package services.socket

import java.util.UUID

import models._
import models.engine.EngineQueries
import models.queries.dynamic.DynamicQuery
import models.queries.result.CachedResultQueries
import models.query.{QueryResult, RowDataOptions}
import models.schema.{ForeignKey, PrimaryKey}
import services.database.DatabaseWorkerPool
import services.database.core.{MasterDatabase, ResultCacheDatabase}
import services.schema.SchemaService
import utils.{DateUtils, ExceptionUtils, JdbcUtils, Logging}

trait DataHelper extends Logging { this: SocketService =>
  protected[this] def handleShowDataResponse(
    queryId: UUID, t: String, name: String, pk: Option[PrimaryKey], keys: Seq[ForeignKey], options: RowDataOptions, resultId: UUID, cacheDb: Boolean
  ) = {
    def work() = {
      val startMs = DateUtils.nowMillis
      val optionsNewLimit = options.copy(limit = options.limit.map(_ + 1))
      val (database, engine) = if (cacheDb) {
        ResultCacheDatabase.conn -> ResultCacheDatabase.conn.engine
      } else {
        activeTransaction.getOrElse(db) -> db.engine
      }
      val (sql, values: Seq[Any]) = EngineQueries.selectFrom(name, optionsNewLimit)(engine)
      log.info(s"Showing data for [$name] using sql [$sql] with values [${values.mkString(", ")}].")
      JdbcUtils.sqlCatch(queryId, sql, startMs, resultId, 0) { () =>
        val result = database.query(DynamicQuery(sql, values))

        val (trimmedData, moreRowsAvailable) = options.limit match {
          case Some(limit) if result.data.size > limit => result.data.take(limit) -> true
          case _ => result.data -> false
        }
        val columnsWithPrimaryKey = result.cols.map { col =>
          pk match {
            case Some(k) => col.copy(primaryKey = k.columns.exists(_.compareToIgnoreCase(col.name) == 0))
            case None => col
          }
        }
        val columnsWithRelations = columnsWithPrimaryKey.map { col =>
          keys.find(_.references.exists(_.source.compareToIgnoreCase(col.name) == 0)) match {
            case Some(fk) => col.copy(
              relationTable = Some(fk.targetTable),
              relationColumn = fk.references.find(_.source.compareToIgnoreCase(col.name) == 0).map(_.target)
            )
            case None => col
          }
        }

        val qr = QueryResult(
          queryId = queryId,
          sql = sql,
          columns = columnsWithRelations,
          data = trimmedData,
          rowsAffected = trimmedData.length,
          moreRowsAvailable = moreRowsAvailable,
          source = Some(options.toSource(t, name)),
          occurred = startMs
        )

        val durationMs = (DateUtils.nowMillis - startMs).toInt
        QueryResultResponse(resultId, 0, qr, durationMs)
      }
    }
    def onSuccess(rm: ResponseMessage) = out ! rm
    def onFailure(t: Throwable) = ExceptionUtils.actorErrorFunction(out, "ShowDataError", t)
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }
}
