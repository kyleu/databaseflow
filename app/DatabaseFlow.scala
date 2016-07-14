import gui.ui.TopFrame
import utils.web.WebApplication

import scala.swing._

object DatabaseFlow extends SimpleSwingApplication {
  System.setProperty("apple.laf.useScreenMenuBar", "true")
  System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Database Flow")

  val app = new WebApplication()
  override val top = new TopFrame(app)

  new Thread(new Runnable {
    override def run() = {
      app.start()
      top.onStart()
    }
  }).start()
}
