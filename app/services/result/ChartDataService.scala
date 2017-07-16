package services.result

import java.util.UUID

import akka.actor.ActorRef
import models.database.Transaction
import models.engine.EngineQueries
import models.queries.dynamic.DynamicQuery
import models.query.QueryResult
import models.user.User
import models.{ChartDataRequest, ChartDataResponse}
import services.database.DatabaseRegistry
import services.database.core.ResultCacheDatabase
import util.Logging

object ChartDataService extends Logging {
  def handleChartDataRequest(
    cdr: ChartDataRequest,
    user: User,
    connectionId: UUID,
    out: ActorRef,
    activeTransaction: Option[Transaction]
  ) = {
    val db = if (cdr.source.t == QueryResult.SourceType.Cache) {
      ResultCacheDatabase.conn
    } else {
      DatabaseRegistry.databaseForUser(user, connectionId) match {
        case Right(x) => x
        case Left(x) => throw x
      }
    }

    val (sql, values: Seq[Any]) = EngineQueries.selectFrom(cdr.source.name, Nil, cdr.source.asRowDataOptions(None))(db.engine)
    val q = new DynamicQuery(sql, values)

    val startMs = System.currentTimeMillis
    val result = activeTransaction match {
      case Some(t) => t.query(q)
      case None => db.query(q)
    }
    val elapsedMs = (System.currentTimeMillis - startMs).toInt

    val mappedData = result.data.map(_.map(_.map(DynamicQuery.transform)))

    val msg = ChartDataResponse(cdr.chartId, result.cols, mappedData, elapsedMs)
    out ! msg
    msg
  }
}
