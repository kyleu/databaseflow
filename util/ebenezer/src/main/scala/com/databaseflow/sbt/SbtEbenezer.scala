package com.databaseflow.sbt

import sbt._
import sbt.Keys._
import com.twitter.scrooge.ScroogeSBT.autoImport._
import services.scalaexport.ExportFiles

object SbtEbenezer extends AutoPlugin {
  override def requires = com.twitter.scrooge.ScroogeSBT

  object autoImport {
    val ebenezer = TaskKey[Seq[File]](
      "ebenezer",
      "Generate better code from Thrift files using Database Flow"
    )
  }

  import autoImport._

  val ebenezerSettings: Seq[Setting[_]] = Seq(
    ebenezer := {
      val startMs = System.currentTimeMillis

      val streamValue = streams.value
      def log(s: String) = streamValue.log.info(s)

      val outputFolder = (sourceManaged in Compile).value
      val thriftSources = scroogeThriftSources.value
      val thriftIncludes = scroogeThriftIncludes.value
      val thriftNamespaceMap = scroogeThriftNamespaceMap.value

      if ( /* scroogeIsDirty.value && */ thriftSources.nonEmpty) {
        log(s"Database Flow code generation is running for [${thriftSources.size}] thrift sources, saving result to [${outputFolder.getPath}]...")
        log(s"Processing [${thriftSources.mkString(", ")}]")
        val loc = IO.createTemporaryDirectory
        ExportFiles.rootLocation = loc.getAbsolutePath
        val result = compile(streamValue.log, outputFolder, thriftSources.toSet, thriftIncludes.toSet, thriftNamespaceMap)
        log(s"Code generation completed in [${System.currentTimeMillis - startMs}ms]")
        log(s"Exported:")
        result.foreach(f => log("  - " + f))
        result
      } else {
        Nil
      }
    },
    sourceGenerators += ebenezer.taskValue
  )

  override lazy val projectSettings = inConfig(Test)(ebenezerSettings) ++ inConfig(Compile)(ebenezerSettings)

  private[this] def compile(log: Logger, outputDir: File, thriftFiles: Set[File], thriftIncludes: Set[File], namespaceMappings: Map[String, String]) = {
    outputDir.mkdirs()

    val result = thriftFiles.toIndexedSeq.map { f =>
      services.scalaexport.ScalaExport.exportThrift(Some(f.getAbsolutePath), Some(outputDir.getAbsolutePath), Set.empty)
    }

    println(result.toString)

    val file = outputDir / "Foo.scala"
    IO.write(file, """object Test extends App { println("Hi") }""")
    Seq(file)
  }
}
