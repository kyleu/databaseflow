import web.WebApplication

import scala.swing._

object DatabaseFlowMain extends SimpleSwingApplication {
  val app = new WebApplication()

  override def top = new MainFrame {
    title = "Database Flow"

    contents = {
      new BoxPanel(Orientation.Vertical) {
        contents += new Label("Database Flow", None.orNull, Alignment.Center) {
          preferredSize = new Dimension(200, 70)
          horizontalAlignment = Alignment.Center
        }
      }
    }

    preferredSize = new Dimension(200, 70)

    override def close() = {
      app.server.stop()
      super.close()
    }
  }
}
