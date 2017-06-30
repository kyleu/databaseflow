import sbt.Keys._
import sbt._

object Database {
  private[this] val dependencies = {
    import Dependencies._
    Seq(Jdbc.hikariCp, Jdbc.h2, Jdbc.mysql, Jdbc.postgres, Jdbc.sqlite, Jdbc.sqlServer)
  }

  private[this] lazy val dblibsSettings = Shared.commonSettings ++ Seq(name := "Database Library", libraryDependencies ++= dependencies)

  lazy val dblibs = Project(id = "dblibs", base = file("dblibs")).settings(dblibsSettings: _*)
}
