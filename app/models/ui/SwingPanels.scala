package models.ui

import javax.swing.BorderFactory

import scala.swing._

object SwingPanels {
  val titleLabel = new Label("Database Flow", None.orNull, Alignment.Left) {
    font = CustomFonts.titleText
    foreground = Colors.white
  }

  val logoLabel = new Label("\uf1c0", None.orNull, Alignment.Left) {
    font = CustomFonts.icons
    foreground = Colors.white
    border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
  }

  val textPanel = new BorderPanel {
    layout(logoLabel) = BorderPanel.Position.West
    layout(titleLabel) = BorderPanel.Position.Center
    background = Colors.bluegrey
  }

  val titlePanel = new GridBagPanel {
    val c = new Constraints
    c.anchor = GridBagPanel.Anchor.Center
    layout(textPanel) = c
    background = Colors.bluegrey
  }

  val textArea = new TextArea {
    font = CustomFonts.regularText
    border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    wordWrap = true
    editable = false
  }

  val consoleScroll = new ScrollPane(textArea) {
    border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
  }

  val statusLabel = new Label("Open Browser", None.orNull, Alignment.Left) {
    font = CustomFonts.largeText
    foreground = Colors.black
    background = Colors.white
    border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
    xAlignment = Alignment.Center
  }

  val statusPanel = new GridBagPanel {
    val c = new Constraints
    c.anchor = GridBagPanel.Anchor.Center
    layout(statusLabel) = c
  }
}
