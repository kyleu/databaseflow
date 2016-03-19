import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

object Database {
  private[this] val dependencies = {
    import Dependencies._
    Seq(
      Logging.slf4jApi, Jdbc.hikariCp,
      Jdbc.h2, Jdbc.mysql, Jdbc.postgres
    )
  }

  private[this] lazy val dblibsSettings = Shared.commonSettings ++ Seq(
    name := "Database Library",
    libraryDependencies ++= dependencies
  )

  lazy val dblibs = Project(
    id = "dblibs",
    base = file("dblibs")
  )
    .settings(dblibsSettings: _*)
    .aggregate(Shared.sharedJvm)
    .dependsOn(Shared.sharedJvm)
}
