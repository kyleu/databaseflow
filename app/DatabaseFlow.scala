import gui.ui.TopFrame
import gui.utils.InitialProperties
import utils.web.WebApplication

import scala.swing._

object DatabaseFlow extends SimpleSwingApplication {
  InitialProperties.set()
  val app = new WebApplication()
  override val top = new TopFrame(app)

  new Thread(new Runnable {
    override def run() = {
      app.start()
      top.onStart()
    }
  }).start()
}
