import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import com.typesafe.sbt.SbtScalariform.{ ScalariformKeys, defaultScalariformSettings }
import net.virtualvoid.sbt.graph.Plugin.graphSettings
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import playscalajs.ScalaJSPlay
import playscalajs.ScalaJSPlay.autoImport._
import sbt.Keys._
import sbt._

object Client {
  lazy val client = (project in file("client")).settings(
    scalaVersion := Shared.Versions.scala,
    persistLauncher := false,
    sourceMapsDirectories += Shared.sharedJs.base / "..",
    unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
    libraryDependencies ++= Seq("org.scala-js" %%% "scalajs-dom" % "0.9.0"),
    scalaJSStage in Global := FastOptStage,
    scapegoatIgnoredFiles := Seq(".*/JsonUtils.scala", ".*/JsonSerializers.scala"),
    scapegoatVersion := Dependencies.scapegoatVersion,
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
  )
    .settings(graphSettings: _*)
    .settings(defaultScalariformSettings: _*)
    .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
    .dependsOn(Shared.sharedJs)
}
