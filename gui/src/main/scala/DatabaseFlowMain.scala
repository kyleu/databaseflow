import ui.TopFrame
import utils.{ InitialProperties, Logging }
import web.WebApplication

import scala.swing._

object DatabaseFlowMain extends SimpleSwingApplication with Logging {
  InitialProperties.set()
  val app = new WebApplication()
  override def top = new TopFrame(app)

  new Thread(new Runnable {
    override def run() = {
      app.start()
    }
  }).start()
}
