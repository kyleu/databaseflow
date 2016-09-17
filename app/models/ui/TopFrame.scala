package models.ui

import java.net.URI
import javax.swing.BorderFactory

import scala.swing._
import scala.swing.event.MouseClicked

class TopFrame() extends MainFrame {
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

  private[this] val statusLabel = new Label("Open Browser", None.orNull, Alignment.Left) {
    font = CustomFonts.regularText
    foreground = Colors.black
    background = Colors.white
    border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
    xAlignment = Alignment.Center
  }

  private[this] val statusPanel = new BorderPanel {
    layout(statusLabel) = BorderPanel.Position.North
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
  }

  contents = panel

  size = new Dimension(320, 140)

  peer.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE)

  background = Colors.white

  listenTo(statusLabel.mouse.clicks)
  reactions += {
    case x: MouseClicked => if (java.awt.Desktop.isDesktopSupported) {
      java.awt.Desktop.getDesktop.browse(new URI("http://localhost:4260"))
    }
  }

  override def closeOperation() = {
    sys.exit(0)
  }
}
