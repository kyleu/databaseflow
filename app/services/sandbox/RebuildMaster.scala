package services.sandbox

import services.data.{MasterDdl, SeedData}
import services.database.MasterDatabase
import utils.ApplicationContext

import scala.concurrent.Future

object RebuildMaster extends SandboxTask {
  override def id = "rebuildmaster"
  override def name = "Rebuild Master Database"

  override def description = ""

  override def run(ctx: ApplicationContext) = {
    MasterDdl.wipe(MasterDatabase.conn)
    MasterDdl.update(MasterDatabase.conn)
    SeedData.insert()
    Future.successful("Rebuilt!")
  }
}
