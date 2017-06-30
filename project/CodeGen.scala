import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

object CodeGen {
  private[this] val dependencies = Seq(Dependencies.Utils.commonsIo, Dependencies.Utils.enumeratum, Dependencies.Hibernate.core)

  private[this] lazy val codegenSettings = Shared.commonSettings ++ Seq(name := "Code Generator", libraryDependencies ++= dependencies)

  lazy val codegen = Project(
    id = "codegen",
    base = file("codegen")
  ).settings(codegenSettings: _*).aggregate(Database.dblibs).dependsOn(Database.dblibs).aggregate(Shared.sharedJvm).dependsOn(Shared.sharedJvm)
}
