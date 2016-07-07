package services.sandbox

import services.data.{MasterDdl, SeedData}
import services.database.MasterDatabaseConnection
import utils.ApplicationContext
import utils.cache.CacheService

import scala.concurrent.Future

object RebuildMaster extends SandboxTask {
  override def id = "rebuildmaster"
  override def name = "Rebuild Master Database"

  override def description = ""

  override def run(ctx: ApplicationContext) = {
    MasterDdl.wipe(MasterDatabaseConnection.conn)
    MasterDdl.update(MasterDatabaseConnection.conn)
    SeedData.insert()
    CacheService.clear()
    Future.successful("Rebuilt!")
  }
}
