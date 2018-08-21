package com.databaseflow.services.scalaexport.thrift

import better.files._
import com.facebook.swift.parser.ThriftIdlParser
import com.google.common.base.Charsets
import com.google.common.io.Files
import com.databaseflow.models.scalaexport.thrift._
import com.databaseflow.services.scalaexport.ExportFiles
import com.databaseflow.services.scalaexport.db.ExportMerge

import scala.concurrent.ExecutionContext

object ThriftParseService {
  private[this] def parse(file: File, flags: Set[String], configLocation: String, depPrefix: String): ThriftParseResult = {
    import scala.collection.JavaConverters._

    val src = Files.asByteSource(file.toJava).asCharSource(Charsets.UTF_8)
    val doc = ThriftIdlParser.parseThriftIdl(src)
    val h = doc.getHeader
    val d = doc.getDefinitions.asScala

    val pkg = Option(h.getNamespace("scala")).filterNot(_.isEmpty).orElse(Option(h.getNamespace("java")).filterNot(_.isEmpty)).getOrElse {
      throw new IllegalStateException("No package")
    }

    val includes = h.getIncludes.asScala
    val included = includes.map { inc =>
      val f = file.parent / inc
      if (f.exists) {
        parse(f, flags, configLocation, depPrefix)
      } else {
        val other = file.parent / (inc + ".include")
        parse(other, flags, configLocation, depPrefix)
      }
    }
    ThriftParseResult(
      filename = file.name,
      srcPkg = pkg.split('.'),
      decls = d,
      includes = included,
      lines = file.lines.toSeq,
      flags = flags,
      configLocation = configLocation,
      depPrefix = depPrefix
    )
  }

  def exportThrift(
    filename: String, persist: Boolean = false, projectLocation: Option[String] = None,
    flags: Set[String] = Set.empty, configLocation: String, depPrefix: String
  )(implicit ec: ExecutionContext): (Map[String, Int], Seq[(String, String)]) = {
    if (filename == "all") {
      val root = File("./tmp/thrift")
      if (!root.exists) {
        throw new IllegalStateException(s"Cannot read [${root.pathAsString}].")
      }
      val results = root.children.flatMap {
        case f if f.isDirectory => f.children.flatMap {
          case c if c.name.endsWith(".thrift") => Some(exportThrift(c.pathAsString, persist, projectLocation, flags, configLocation, depPrefix))
          case _ => None
        }
        case f if f.name.endsWith(".thrift") => Seq(exportThrift(f.pathAsString, persist, projectLocation, flags, configLocation, depPrefix))
        case _ => Nil
      }.toSeq

      val map = results.map(_._1).foldLeft(Map.empty[String, Int])((l, r) => (l.keys ++ r.keys).map { k =>
        k -> (l.getOrElse(k, 0) + r.getOrElse(k, 0))
      }.toMap)
      val seq = results.map(_._2).foldLeft(Seq.empty[(String, String)])((l, r) => l ++ r)
      map -> seq
    } else {
      val result = parse(better.files.File(filename), flags, configLocation, depPrefix)
      ExportFiles.persistThrift(result, ExportFiles.prepareRoot(persist))

      if (persist) {
        val rootDir = projectLocation match {
          case Some(l) => l.toFile
          case None => s"./tmp/tempoutput".toFile
        }
        val coreDir = rootDir
        ExportMerge.mergeDirectories(None, "N/A", coreDir, rootDir, Nil, s => println(s)) -> result.allFiles.map(f => f.path -> f.rendered)
      } else {
        Map.empty[String, Int] -> result.allFiles.map(f => f.path -> f.rendered)
      }
    }
  }
}
