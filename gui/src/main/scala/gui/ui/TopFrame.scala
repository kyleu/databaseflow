package gui.ui

import gui.web.WebApp

import scala.swing._

class TopFrame(app: WebApp) extends MainFrame {
  title = "Database Flow"
  resizable = false

  private[this] val titleLabel = new Label("Database Flow", None.orNull, Alignment.Center) {
    font = CustomFonts.titleText
    foreground = Colors.foreground
    peer.setSize(new Dimension(300, 40))
    peer.setLocation(0, 20)
  }

  private[this] val statusLabel = new Label("Starting...", None.orNull, Alignment.Center) {
    font = CustomFonts.regularText
    foreground = Colors.foreground
    peer.setSize(new Dimension(300, 40))
    peer.setLocation(0, 95)
  }

  private[this] val panel = new Panel {
    peer.setLayout(None.orNull)

    background = Colors.background

    def add(comp: Component) = {
      peer.add(comp.peer)
    }
  }

  panel.add(titleLabel)
  panel.add(statusLabel)

  contents = panel

  menuBar = new FrameMenu()

  size = new Dimension(300, 200)

  background = Colors.background

  peer.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE)

  def onStart() = {
    statusLabel.text = "Started"
  }

  override def closeOperation() = {
    if (app.started) {
      app.stop()
    }
    sys.exit(0)
  }
}
