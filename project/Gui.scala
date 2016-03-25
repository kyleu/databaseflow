import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import sbt.Keys._
import sbt._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.jdkpackager.JDKPackagerPlugin
import com.typesafe.sbt.packager.linux.LinuxPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.universal.UniversalPlugin
import com.typesafe.sbt.packager.windows.WindowsPlugin

object Gui {
  private[this] val dependencies = {
    import Dependencies._
    Seq(Ui.swing)
  }

  private[this] lazy val guiSettings = Shared.commonSettings ++ Seq(
    name := "database-flow-standalone",
    libraryDependencies ++= dependencies,
    mainClass in Compile := Some("DatabaseFlowMain"),

    packageSummary in Linux := "Database Flow",
    packageSummary in Windows := "Database Flow",
    packageDescription := " A description of your project",

    maintainer in Windows := "Database Flow",
    maintainer in Debian := "Database Flow <feedback@databaseflow.com>"

  )

  lazy val gui = Project(
    id = "gui",
    base = file("gui")
  )
    .settings(guiSettings: _*)
    .enablePlugins(JavaAppPackaging)
    .enablePlugins(UniversalPlugin, LinuxPlugin, DebianPlugin, RpmPlugin, DockerPlugin, WindowsPlugin, JDKPackagerPlugin)
    .aggregate(Server.server)
    .dependsOn(Server.server)
}
