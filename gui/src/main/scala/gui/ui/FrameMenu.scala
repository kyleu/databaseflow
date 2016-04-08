package gui.ui

import scala.swing._

class FrameMenu extends MenuBar {
  contents += new Menu("Server") {
    contents += new MenuItem(Action("Start") {

    })

    contents += new MenuItem(Action("Stop") {

    })

    contents += new Separator

    contents += new MenuItem(Action("Restart") {

    })
  }
}
