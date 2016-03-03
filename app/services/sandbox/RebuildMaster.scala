package services.sandbox

import services.database.{SeedData, MasterDatabase, Schema}
import utils.ApplicationContext

import scala.concurrent.Future

object RebuildMaster extends SandboxTask {
  override def id = "rebuildmaster"
  override def name = "Rebuild Master Database"

  override def description = ""

  override def run(ctx: ApplicationContext) = {
    Schema.wipe(MasterDatabase.db)
    Schema.update(MasterDatabase.db)
    SeedData.insert(MasterDatabase.db)
    Future.successful("Rebuilt!")
  }
}
