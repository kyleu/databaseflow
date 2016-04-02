import models.gui.ui.TopFrame
import models.gui.utils.InitialProperties
import models.gui.web.WebApplication

import scala.swing._

object DatabaseFlow extends SimpleSwingApplication {
  InitialProperties.set()
  val app = new WebApplication()
  override def top = new TopFrame(app)

  new Thread(new Runnable {
    override def run() = {
      app.start()
    }
  }).start()
}
