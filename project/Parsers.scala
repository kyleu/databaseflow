import Dependencies._
import com.typesafe.sbt.SbtScalariform.{ ScalariformKeys, scalariformSettings }
import net.virtualvoid.sbt.graph.DependencyGraphSettings.graphSettings
import sbt.Keys._
import sbt._

object Parsers {
  lazy val git = (project in file("parse/git"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(libraryDependencies ++= Seq(Utils.joda, Utils.enumeratum, Utils.git))
    .settings(Shared.commonSettings: _*)
    .settings(graphSettings: _*)
    .settings(scalariformSettings: _*)

  lazy val rescuetime = (project in file("parse/rescuetime"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(libraryDependencies ++= Seq(Play.playWs, Utils.enumeratum, Utils.git))
    .settings(Shared.commonSettings: _*)
    .settings(graphSettings: _*)
    .settings(scalariformSettings: _*)

  lazy val slack = (project in file("parse/slack"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(libraryDependencies ++= Seq(Play.playWs, Utils.enumeratum, Utils.git))
    .settings(Shared.commonSettings: _*)
    .settings(graphSettings: _*)
    .settings(scalariformSettings: _*)

  lazy val trello = (project in file("parse/trello"))
    .settings(ScalariformKeys.preferences := ScalariformKeys.preferences.value)
    .settings(libraryDependencies ++= Seq(Play.playWs, Utils.enumeratum, Utils.git))
    .settings(Shared.commonSettings: _*)
    .settings(graphSettings: _*)
    .settings(scalariformSettings: _*)
}
