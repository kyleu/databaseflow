package services.sandbox

import services.database.{ SeedData, MasterDatabase, MasterDdl }
import utils.ApplicationContext

import scala.concurrent.Future

object RebuildMaster extends SandboxTask {
  override def id = "rebuildmaster"
  override def name = "Rebuild Master Database"

  override def description = ""

  override def run(ctx: ApplicationContext) = {
    MasterDdl.wipe(MasterDatabase.db)
    MasterDdl.update(MasterDatabase.db)
    SeedData.insert(MasterDatabase.db)
    Future.successful("Rebuilt!")
  }
}
