package models.ui

import java.net.URI
import javax.swing.JOptionPane

import utils.Logging

import scala.swing._
import scala.swing.event.MouseClicked

object TopFrame {
  var frame: Option[TopFrame] = None
  def open() = {
    frame.foreach(_ => throw new IllegalStateException("TopFrame already started."))
    frame = Some(new TopFrame())
    frame.foreach(_.open())
  }
}

class TopFrame() extends MainFrame {
  def log(s: String) = SwingPanels.textArea.append(s + "\n")
  def error(msg: String) = JOptionPane.showMessageDialog(None.orNull, msg)

  title = "Database Flow"
  resizable = false
  iconImage = toolkit.getImage(getClass.getResource("/icon.png"))

  private[this] val panel = new BorderPanel {
    layout(SwingPanels.titlePanel) = BorderPanel.Position.North
    layout(SwingPanels.consoleScroll) = BorderPanel.Position.Center
    layout(SwingPanels.statusPanel) = BorderPanel.Position.South
  }

  contents = panel
  size = new Dimension(640, 320)
  background = Colors.white

  listenTo(SwingPanels.statusLabel.mouse.clicks)
  reactions += {
    case x: MouseClicked => if (java.awt.Desktop.isDesktopSupported) {
      java.awt.Desktop.getDesktop.browse(new URI("http://localhost:4260"))
    }
  }

  Logging.setCallback((level: Int, message: String) => if (level > 1) { log(message) })

  peer.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE)
  override def closeOperation() = sys.exit(0)
}
