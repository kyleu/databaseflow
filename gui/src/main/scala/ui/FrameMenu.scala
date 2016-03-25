package ui

import scala.swing._

class FrameMenu extends MenuBar {
  contents += new Menu("Server") {
    contents += new MenuItem(Action("Start") {
      println("Start invoked.")
    })
    contents += new MenuItem(Action("Stop") {
      println("Stop invoked.")
    })
    contents += new Separator
    contents += new MenuItem(Action("Restart") {
      println("Restart invoked.")
    })
  }
}
