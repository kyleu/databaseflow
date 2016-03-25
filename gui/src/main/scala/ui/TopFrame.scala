package ui

import web.WebApplication

import scala.swing._

class TopFrame(app: WebApplication) extends MainFrame {
  title = "Database Flow"

  private[this] val titleLabel = new Label("Database Flow", None.orNull, Alignment.Center) {
    font = RobotoFont.titleText
    foreground = Colors.titleForeground
  }

  private[this] val statusLabel = new Label("Starting application...", None.orNull, Alignment.Center) {
    font = RobotoFont.regularText
    foreground = Colors.panelForeground
  }

  private[this] val detailPanel = new BorderPanel {
    layout(statusLabel) = BorderPanel.Position.Center
    background = Colors.panelBackground
    border = Swing.EmptyBorder(5, 5, 5, 5)
  }

  private[this] val borderPanel = new BorderPanel {
    layout(detailPanel) = BorderPanel.Position.Center
    border = Swing.EmptyBorder(20, 0, 0, 0)
    background = Colors.background
  }

  contents = new BorderPanel {
    layout(titleLabel) = BorderPanel.Position.North
    layout(borderPanel) = BorderPanel.Position.Center
    background = Colors.background
    border = Swing.EmptyBorder(10, 20, 20, 20)
  }

  menuBar = new FrameMenu()

  size = new Dimension(300, 200)

  background = Colors.background

  override def close() = {
    if (app.started) {
      app.stop()
    }
    super.close()
  }
}
