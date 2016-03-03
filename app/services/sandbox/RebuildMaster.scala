package services.sandbox

import services.database.{SeedData, MasterDatabase, MasterSchema}
import utils.ApplicationContext

import scala.concurrent.Future

object RebuildMaster extends SandboxTask {
  override def id = "rebuildmaster"
  override def name = "Rebuild Master Database"

  override def description = ""

  override def run(ctx: ApplicationContext) = {
    MasterSchema.wipe(MasterDatabase.db)
    MasterSchema.update(MasterDatabase.db)
    SeedData.insert(MasterDatabase.db)
    Future.successful("Rebuilt!")
  }
}
