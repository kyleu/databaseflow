import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

object CodeGen {
  private[this] val dependencies = {
    import Dependencies._
    Seq(Utils.commonsIo, Utils.enumeratum, Hibernate.core)
  }

  private[this] lazy val codegenSettings = Shared.commonSettings ++ Seq(
    name := "Code Generator",
    libraryDependencies ++= dependencies
  )

  lazy val codegen = Project(
    id = "codegen",
    base = file("codegen")
  )
    .settings(codegenSettings: _*)
    .aggregate(Database.dblibs)
    .dependsOn(Database.dblibs)
    .aggregate(Shared.sharedJvm)
    .dependsOn(Shared.sharedJvm)
}
