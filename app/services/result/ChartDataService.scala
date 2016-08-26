package services.result

import java.util.UUID

import akka.actor.ActorRef
import models.database.Transaction
import models.engine.EngineQueries
import models.query.QueryResult.Source
import models.user.User
import services.database.DatabaseRegistry
import services.database.core.ResultCacheDatabase
import utils.Logging

object ChartDataService extends Logging {
  def handleChartDataRequest(id: UUID, user: User, connectionId: UUID, source: Source, out: ActorRef, activeTransaction: Option[Transaction]) = {
    val db = if (source.t == "cache") {
      ResultCacheDatabase.conn
    } else {
      DatabaseRegistry.databaseFor(user, connectionId) match {
        case Right(x) => x
        case Left(x) => throw x
      }
    }

    val sql = EngineQueries.selectFrom(source.name, source.asRowDataOptions)(db.engine)

    log.info("Handling chart data request!")
    //db.query()
  }
}
