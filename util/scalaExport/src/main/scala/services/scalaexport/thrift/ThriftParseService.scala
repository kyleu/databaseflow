package services.scalaexport.thrift

import better.files._
import com.facebook.swift.parser.ThriftIdlParser
import com.google.common.base.Charsets
import com.google.common.io.Files
import models.scalaexport.db.config.ExportConfiguration
import models.scalaexport.thrift._
import services.scalaexport.db.{ExportFiles, ExportMerge}

import scala.concurrent.ExecutionContext

object ThriftParseService {
  private[this] def parse(file: File): ThriftParseResult = {
    import scala.collection.JavaConverters._

    val src = Files.asByteSource(file.toJava).asCharSource(Charsets.UTF_8)
    val doc = ThriftIdlParser.parseThriftIdl(src)
    val h = doc.getHeader
    val d = doc.getDefinitions.asScala

    val pkg = Option(h.getNamespace("scala")).filterNot(_.isEmpty).orElse(Option(h.getNamespace("java")).filterNot(_.isEmpty)).getOrElse {
      throw new IllegalStateException("No package")
    }

    val includes = h.getIncludes.asScala
    val included = includes.map(inc => parse(file.parent / inc))
    ThriftParseResult(
      filename = file.name,
      srcPkg = pkg.split('.'),
      decls = d,
      includes = included,
      lines = file.lines.toSeq
    )
  }

  def exportThrift(filename: String, persist: Boolean = false, projectLocation: Option[String] = None)(
    implicit
    ec: ExecutionContext
  ): (Map[String, Int], Seq[(String, String)]) = {
    if (filename == "all") {
      val root = File("./tmp/thrift")
      if (!root.exists) {
        throw new IllegalStateException(s"Cannot read [${root.pathAsString}].")
      }
      val results = root.children.flatMap {
        case f if f.isDirectory => f.children.flatMap {
          case c if c.name.endsWith(".thrift") => Some(exportThrift(c.pathAsString, persist, projectLocation))
          case _ => None
        }
        case f if f.name.endsWith(".thrift") => Seq(exportThrift(f.pathAsString, persist, projectLocation))
        case _ => Nil
      }.toSeq

      val map = results.map(_._1).foldLeft(Map.empty[String, Int])((l, r) => (l.keys ++ r.keys).map {
        case k if k == "same-content" => k -> Math.max(l.getOrElse(k, 0), r.getOrElse(k, 0))
        case k => k -> (l.getOrElse(k, 0) + r.getOrElse(k, 0))
      }.toMap)
      val seq = results.map(_._2).foldLeft(Seq.empty[(String, String)])((l, r) => l ++ r)
      map -> seq
    } else {
      val result = parse(better.files.File(filename))
      val injected = if (persist) {
        ExportFiles.persistThrift(result, ExportFiles.prepareRoot())

        val rootDir = projectLocation match {
          case Some(l) => l.toFile
          case None => s"./tmp/tempoutput".toFile
        }

        ExportMerge.merge(None, "N/A", rootDir, Nil, s => println(s)) -> result.allFiles.map(f => f.path -> f.rendered)
      } else {
        Map.empty[String, Int] -> Nil
      }
      injected
    }
  }
}
