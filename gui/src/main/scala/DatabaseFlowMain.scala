import ui.TopFrame
import utils.InitialProperties
import web.WebApplication

import scala.swing._

object DatabaseFlowMain extends SimpleSwingApplication {
  InitialProperties.set()
  val app = new WebApplication()
  override def top = new TopFrame(app)
}
