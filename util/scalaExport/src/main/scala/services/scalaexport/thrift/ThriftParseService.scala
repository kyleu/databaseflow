package services.scalaexport.thrift

import better.files.File
import com.facebook.swift.parser.ThriftIdlParser
import com.facebook.swift.parser.model._
import com.google.common.base.Charsets
import com.google.common.io.Files
import models.scalaexport.OutputFile
import models.scalaexport.thrift.{ThriftIntegerEnum, ThriftService, ThriftStringEnum, ThriftStruct}
import services.scalaexport.file.thrift._

object ThriftParseService {
  case class Result(filename: String, srcPkg: Seq[String], decls: Seq[Definition], includes: Seq[Result]) {
    lazy val tgtPkg = if (srcPkg.lastOption.contains("thrift")) { srcPkg.dropRight(1) } else { srcPkg }
    lazy val pkgMap: Map[String, Seq[String]] = ((filename.stripSuffix(".thrift") -> tgtPkg) +: includes.flatMap(r => r.pkgMap.toSeq)).toMap

    lazy val typedefs = decls.filter(_.isInstanceOf[Typedef]).map(_.asInstanceOf[Typedef]).map { t =>
      t.getName -> (t.getType match {
        case i: IdentifierType => i.getName
        case b: BaseType => b.getType.toString
      })
    }.toMap

    lazy val stringEnums = decls.filter(_.isInstanceOf[StringEnum]).map(_.asInstanceOf[StringEnum]).map(ThriftStringEnum.apply)
    lazy val stringEnumNames = stringEnums.map(_.name)
    lazy val stringEnumString = stringEnums.map(e => s"  ${e.name} (${e.values.size} values)").mkString("\n")
    lazy val stringEnumFiles = stringEnums.flatMap(e => Seq(ThriftEnumFile.exportString(srcPkg, tgtPkg, e)))

    lazy val intEnums = decls.filter(_.isInstanceOf[IntegerEnum]).map(_.asInstanceOf[IntegerEnum]).map(ThriftIntegerEnum.apply)
    lazy val intEnumNames = intEnums.map(_.name)
    lazy val intEnumString = intEnums.map(e => s"  ${e.name} (${e.fields.size} values)").mkString("\n")
    lazy val intEnumFiles = intEnums.flatMap(e => Seq(ThriftEnumFile.exportInt(srcPkg, tgtPkg, e)))

    lazy val enumDefaults = (stringEnums.map(e => e.name -> e.values.head) ++ intEnums.map(e => e.name -> e.fields.head._1)).toMap

    lazy val structs = decls.filter(_.isInstanceOf[Struct]).map(_.asInstanceOf[Struct]).map(ThriftStruct.apply)
    lazy val structNames = structs.map(_.name)
    lazy val structString = structs.map(struct => s"  ${struct.name} (${struct.fields.size} fields)").mkString("\n")
    lazy val structFiles = structs.flatMap(struct => Seq(ThriftModelFile.export(srcPkg, tgtPkg, struct, typedefs, enumDefaults, pkgMap)))

    lazy val services = decls.filter(_.isInstanceOf[Service]).map(_.asInstanceOf[Service]).map(ThriftService.apply)
    lazy val allServices = includes.flatMap(_.services) ++ services
    lazy val serviceNames = services.map(_.name)

    lazy val serviceFiles = services.flatMap(serviceMethodFiles)
    lazy val serviceString = services.map(struct => s"  ${struct.name} (${struct.methods.size} methods)").mkString("\n")

    lazy val files = intEnumFiles ++ stringEnumFiles ++ structFiles ++ serviceFiles
    lazy val allFiles: Seq[OutputFile] = includes.flatMap(_.allFiles) ++ files

    lazy val summaryString = s"""
      |[[[$filename]]]
      |
      |Package: [${srcPkg.mkString(".")}]
      |
      |Models:
      |$structString
      |
      |Services:
      |$serviceString
    """.stripMargin.trim

    override lazy val toString = {
      val incSummary = if (includes.isEmpty) { "" } else { includes.map(_.summaryString).mkString("\n\n") + "\n\n" }
      incSummary + summaryString + s"\n\nFiles:" + allFiles.map(file => "\n\n[" + file.filename + "]\n" + file.rendered).mkString
    }

    private[this] def serviceMethodFiles(service: ThriftService) = {
      val baseFiles = Seq(
        ThriftServiceFile.export(srcPkg, tgtPkg, service, typedefs, enumDefaults, pkgMap),
        ThriftTwirlServiceFile.export(tgtPkg, service, typedefs, pkgMap),
        ThriftControllerFile.export(tgtPkg, service),
        ThriftRoutesFile.export(service),
        ThriftControllerFile.export(tgtPkg, service)
      )
      val methodFiles = service.methods.map { m =>
        ThriftTwirlServiceMethodFile.export(tgtPkg, service, m, typedefs, pkgMap)
      }
      baseFiles ++ methodFiles
    }
  }

  def parse(file: File): Result = {
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
    Result(filename = file.name, srcPkg = pkg.split('.'), decls = d, includes = included)
  }
}
