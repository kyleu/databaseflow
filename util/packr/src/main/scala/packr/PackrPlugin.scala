package packr

import java.io.File
import java.io._
import java.nio.file.Files

import sbt._
import scala.collection.JavaConversions._

object Import {
  val packr = TaskKey[File]("packr", "Run Packr")
}

object PackrPlugin extends AutoPlugin {
  override def trigger = allRequirements

  val autoImport = Import
  import autoImport._
  import sbt.Keys._

  override val projectSettings = Seq(
    packr := runPackr.value
  )

  private def runPackr: Def.Initialize[Task[File]] = Def.task {
    val outputdir = (target in packr).value
    val utilDir = outputdir / "packr"
    val distdir = outputdir / "app"
    val configJson = utilDir / "packr.json"
    val classpath = distdir / "lib"

    IO.delete(distdir)
    distdir.mkdirs()

    IO.delete(utilDir)
    utilDir.mkdirs()

    // copy the project artifact and all dependencies to /lib
    val jars = {
      val filter = configurationFilter(Compile.name)
      val files = (`package` in Compile).value +: update.value.matching(filter)
      IO.copy {
        files.map { file => file -> classpath / file.name }
      }
    }

    val command = Process("java", Seq("-jar", "./util/packr/lib/packr.jar"))
    command.!

    distdir
  }
}
