import sbt.Keys._
import sbt._

object Gui {
  private[this] val dependencies = {
    Seq(Dependencies.Ui.swing)
  }

  private[this] lazy val guiSettings = Shared.commonSettings ++ Seq(
    libraryDependencies ++= dependencies
  )

  lazy val gui = Project(
    id = "gui",
    base = file("gui")
  ).settings(guiSettings: _*)
}
