package services.socket

import java.util.UUID

import akka.actor.ActorRef
import models._
import models.database.Queryable
import models.engine.{DatabaseEngine, EngineQueries}
import models.queries.dynamic.DynamicQuery
import models.query.{QueryResult, RowDataOptions}
import models.schema.{ForeignKey, PrimaryKey}
import services.database.DatabaseWorkerPool
import utils.{DateUtils, ExceptionUtils, JdbcUtils, Logging}

object DataHelper extends Logging {
  case class Params(queryId: UUID, t: String, name: String, pk: Option[PrimaryKey], keys: Seq[ForeignKey], options: RowDataOptions, resultId: UUID)

  def handleShowDataResponse(params: Params, database: Queryable, engine: DatabaseEngine, out: Option[ActorRef]) = {
    def work() = {
      val startMs = DateUtils.nowMillis
      val optionsNewLimit = params.options.copy(limit = params.options.limit.map(_ + 1))
      val (sql, values: Seq[Any]) = EngineQueries.selectFrom(params.name, optionsNewLimit)(engine)
      log.info(s"Showing data for [${params.name}] using sql [$sql] with values [${values.mkString(", ")}].")
      JdbcUtils.sqlCatch(params.queryId, sql, startMs, params.resultId, 0) { () =>
        val result = database.query(DynamicQuery(sql, values))

        val (trimmedData, moreRowsAvailable) = params.options.limit match {
          case Some(limit) if result.data.size > limit => result.data.take(limit) -> true
          case _ => result.data -> false
        }
        val columnsWithPrimaryKey = result.cols.map { col =>
          params.pk match {
            case Some(k) => col.copy(primaryKey = k.columns.exists(_.compareToIgnoreCase(col.name) == 0))
            case None => col
          }
        }
        val columnsWithRelations = columnsWithPrimaryKey.map { col =>
          params.keys.find(_.references.exists(_.source.compareToIgnoreCase(col.name) == 0)) match {
            case Some(fk) => col.copy(
              relationTable = Some(fk.targetTable),
              relationColumn = fk.references.find(_.source.compareToIgnoreCase(col.name) == 0).map(_.target)
            )
            case None => col
          }
        }

        val qr = QueryResult(
          queryId = params.queryId,
          sql = sql,
          columns = columnsWithRelations,
          data = trimmedData,
          rowsAffected = trimmedData.length,
          moreRowsAvailable = moreRowsAvailable,
          source = Some(params.options.toSource(params.t, params.name)),
          occurred = startMs
        )

        val durationMs = (DateUtils.nowMillis - startMs).toInt
        QueryResultResponse(params.resultId, 0, qr, durationMs)
      }
    }
    def onSuccess(rm: ResponseMessage) = out.foreach(_ ! rm)
    def onFailure(t: Throwable) = out.foreach(o => ExceptionUtils.actorErrorFunction(o, "ShowDataError", t))
    DatabaseWorkerPool.submitWork(work, onSuccess, onFailure)
  }
}
