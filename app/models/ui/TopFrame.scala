package models.ui

import java.net.URI
import javax.swing.{BorderFactory, ImageIcon, JOptionPane}

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
  def log(s: String) = textArea.append(s + "\n")
  def error(msg: String) = JOptionPane.showMessageDialog(None.orNull, msg)

  title = "Database Flow"
  resizable = false
  iconImage = toolkit.getImage(getClass.getResource("icon.png"))

  private[this] val titleLabel = new Label("Database Flow", None.orNull, Alignment.Left) {
    font = CustomFonts.titleText
    foreground = Colors.white
  }

  private[this] val logoLabel = new Label("\uf1c0", None.orNull, Alignment.Left) {
    font = CustomFonts.icons
    foreground = Colors.white
    border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
  }

  private[this] val textPanel = new BorderPanel {
    layout(logoLabel) = BorderPanel.Position.West
    layout(titleLabel) = BorderPanel.Position.Center
    background = Colors.bluegrey
  }

  private[this] val titlePanel = new GridBagPanel {
    val c = new Constraints
    c.anchor = GridBagPanel.Anchor.Center
    layout(textPanel) = c
    background = Colors.bluegrey
  }

  private[this] val textArea = new TextArea {
    font = CustomFonts.regularText
    border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    wordWrap = true
    editable = false
  }

  private[this] val consoleScroll = new ScrollPane(textArea) {
    border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
  }

  private[this] val statusLabel = new Label("Open Browser", None.orNull, Alignment.Left) {
    font = CustomFonts.largeText
    foreground = Colors.black
    background = Colors.white
    border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
    xAlignment = Alignment.Center
  }

  private[this] val statusPanel = new GridBagPanel {
    val c = new Constraints
    c.anchor = GridBagPanel.Anchor.Center
    layout(statusLabel) = c
  }

  private[this] val panel = new BorderPanel {
    layout(titlePanel) = BorderPanel.Position.North
    layout(consoleScroll) = BorderPanel.Position.Center
    layout(statusPanel) = BorderPanel.Position.South
  }

  contents = panel

  size = new Dimension(640, 320)

  peer.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE)

  background = Colors.white

  listenTo(statusLabel.mouse.clicks)
  reactions += {
    case x: MouseClicked => if (java.awt.Desktop.isDesktopSupported) {
      java.awt.Desktop.getDesktop.browse(new URI("http://localhost:4260"))
    }
  }

  Logging.setCallback((level: Int, message: String) => if (level > 1) { log(message) })

  override def closeOperation() = sys.exit(0)
}
