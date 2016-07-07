package services.sandbox

import java.util.UUID

import models.queries.result.CreateResultTable
import models.query.QueryResult
import models.result.{CachedResult, CachedResultQuery, CachedResultQueryHelper}
import models.schema.ColumnType._
import services.connection.ConnectionSettingsService
import services.database.{DatabaseRegistry, ResultCacheDatabase}
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
    val connectionId = ConnectionSettingsService.getAll.find(_.name == "PostgreSQL Sample").getOrElse(throw new IllegalStateException()).id
    val owner = None
    val sql = "select * from city"

    val model = CachedResult(resultId, queryId, connectionId, owner, sql = sql)
    CachedResultService.insertCacheResult(model)

    DatabaseRegistry.db(connectionId).executeUnknown(CachedResultQuery(model, None))

    val columns = Seq(
      QueryResult.Col("v", StringType, precision = Some(128)),
      QueryResult.Col("bd", BigDecimalType, precision = Some(4), scale = Some(4)),
      QueryResult.Col("b", BooleanType),
      QueryResult.Col("by", ByteType),
      QueryResult.Col("s", ShortType),
      QueryResult.Col("i", IntegerType),
      QueryResult.Col("l", LongType),
      QueryResult.Col("f", FloatType),
      QueryResult.Col("d", DoubleType),
      QueryResult.Col("ba", ByteArrayType),
      QueryResult.Col("da", DateType),
      QueryResult.Col("ti", TimeType),
      QueryResult.Col("ts", TimestampType),
      QueryResult.Col("r", RefType),
      QueryResult.Col("x", XmlType),
      QueryResult.Col("u", UuidType)

    //QueryResult.Col("n", NullType),
    //QueryResult.Col("o", ObjectType),
    //QueryResult.Col("st", StructType),
    //QueryResult.Col("a", ArrayType),

    //QueryResult.Col("unk", UnknownType)
    )

    val statement = CreateResultTable(resultId, columns)(ResultCacheDatabase.conn.engine).sql

    CachedResultQueryHelper.createResultTable(UUID.randomUUID, columns)

    Future.successful("Ok!")
  }
}
