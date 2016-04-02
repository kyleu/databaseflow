import com.typesafe.sbt.SbtScalariform.{ ScalariformKeys, scalariformSettings }
import net.virtualvoid.sbt.graph.Plugin.graphSettings
import sbt.Keys._
import sbt._

object Utilities {
  lazy val iconCreator = (project in file("util/iconCreator")).settings(
    name := "icon-creator",
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
  )
    .settings(Shared.commonSettings: _*)
    .settings(graphSettings: _*)
    .settings(scalariformSettings: _*)
}
