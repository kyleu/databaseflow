import Dependencies._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

object CodeGen {
  private[this] val dependencies = Seq(Utils.commonsIo, Utils.enumeratum, Utils.betterFiles, Hibernate.core)

  private[this] lazy val codegenSettings = Shared.commonSettings ++ Seq(name := "Code Generator", libraryDependencies ++= dependencies)

  lazy val codegen = Project(
    id = "codegen",
    base = file("codegen")
  ).settings(codegenSettings: _*).aggregate(Database.dblibs).dependsOn(Database.dblibs).aggregate(Shared.sharedJvm).dependsOn(Shared.sharedJvm)
}
