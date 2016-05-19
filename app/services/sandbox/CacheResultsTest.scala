package services.sandbox

import java.util.UUID

import models.engine.rdbms.PostgreSQL
import models.queries.result.CreateResultTable
import services.database.MasterDatabase
import services.result.CachedResultService
import utils.ApplicationContext

import scala.concurrent.Future

object CacheResultsTest extends SandboxTask {
  override def id = "cachetest"
  override def name = "Cache Results Test"
  override def description = "Test caching query results."
  override def isHtml = true

  override def run(ctx: ApplicationContext) = {
    val resultId = UUID.randomUUID
    val queryId = UUID.randomUUID
    val connectionId = UUID.randomUUID
    val owner = None
    val sql = "select * from actor"

    CachedResultService.cache(resultId, queryId, connectionId, owner, sql)

    Future.successful("Ok!")
  }
}
