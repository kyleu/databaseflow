package services.scalaexport.thrift

import better.files.File
import com.facebook.swift.parser.ThriftIdlParser
import com.google.common.base.Charsets
import com.google.common.io.Files
import models.scalaexport.thrift._

object ThriftParseService {
  def parse(file: File): ThriftParseResult = {
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
}
