package gui.ui

import javax.swing.BorderFactory

import gui.web.WebApp

import scala.swing._

class TopFrame(app: WebApp) extends MainFrame {
  title = "Database Flow"
  resizable = false

  private[this] val titleLabel = new Label("Database Flow", None.orNull, Alignment.Left) {
    font = CustomFonts.titleText
    foreground = Colors.white
  }

  private[this] val logoLabel = new Label("\uf1c0", None.orNull, Alignment.Left) {
    font = CustomFonts.icons
    foreground = Colors.white
    border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
  }

  private[this] val statusLabel = new Label("Starting...", None.orNull, Alignment.Left) {
    font = CustomFonts.regularText
    foreground = Colors.black
    background = Colors.white
    border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
  }

  private[this] val linkLabel = new Label("http://localhost:4000", None.orNull, Alignment.Left) {
    font = CustomFonts.regularText
    foreground = Colors.black
    background = Colors.white
    cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
    border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
  }

  private[this] val statusPanel = new BorderPanel {
    layout(statusLabel) = BorderPanel.Position.North
    background = Colors.white
  }

  private[this] val linkPanel = new BorderPanel {
    layout(linkLabel) = BorderPanel.Position.South
    background = Colors.white
  }

  private[this] val titlePanel = new BorderPanel {
    layout(logoLabel) = BorderPanel.Position.West
    layout(titleLabel) = BorderPanel.Position.Center
    background = Colors.bluegrey
  }

  private[this] val panel = new BorderPanel {
    layout(titlePanel) = BorderPanel.Position.North
    layout(statusPanel) = BorderPanel.Position.Center
    layout(linkPanel) = BorderPanel.Position.South
  }

  contents = panel

  menuBar = new FrameMenu()

  size = new Dimension(320, 170)

  peer.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE)

  background = Colors.white

  def onStart() = {
    statusLabel.text = "Server started."
  }

  override def closeOperation() = {
    if (app.started) {
      app.stop()
    }
    sys.exit(0)
  }
}
