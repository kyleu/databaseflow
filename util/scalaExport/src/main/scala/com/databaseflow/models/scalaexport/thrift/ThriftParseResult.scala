package com.databaseflow.models.scalaexport.thrift

import com.facebook.swift.parser.model._
import com.databaseflow.models.scalaexport.file.OutputFile
import com.databaseflow.services.scalaexport.ExportHelper
import com.databaseflow.services.scalaexport.thrift.file._

case class ThriftParseResult(
    filename: String,
    srcPkg: Seq[String],
    decls: Seq[Definition],
    includes: Seq[ThriftParseResult],
    lines: Seq[String],
    flags: Set[String],
    configLocation: String
) {
  lazy val tgtPkg = srcPkg.dropRight(1)
  lazy val pkgMap: Map[String, Seq[String]] = ((filename.stripSuffix(".thrift") -> tgtPkg) +: includes.flatMap(r => r.pkgMap.toSeq)).toMap

  lazy val comments = lines.filter(_.trim.startsWith("#")).map(_.trim.stripPrefix("#").trim)
  lazy val exportModelRoot = comments.find(_.startsWith("exportModelRoot")).map(_.stripPrefix("exportModelRoot").trim.stripPrefix("=").trim)

  lazy val overrides = new ThriftOverrides(configLocation)

  lazy val typedefs = decls.filter(_.isInstanceOf[Typedef]).map(_.asInstanceOf[Typedef]).map { t =>
    t.getName -> (t.getType match {
      case i: IdentifierType => i.getName
      case b: BaseType => b.getType.toString
      case x => throw new IllegalStateException("Cannot handle complex typedefs: " + t)
    })
  }.toMap

  lazy val stringEnums = decls.filter(_.isInstanceOf[StringEnum]).map(_.asInstanceOf[StringEnum]).map(ThriftStringEnum.apply)
  lazy val stringEnumNames = stringEnums.map(_.name)
  lazy val stringEnumString = stringEnums.map(e => s"  ${e.name} (${e.values.size} values)").mkString("\n")
  lazy val stringEnumFiles = stringEnums.flatMap(e => if (flags("simple")) {
    Seq(ThriftStringEnumFile.exportString(srcPkg, tgtPkg, e, exportModelRoot))
  } else if (flags("extras")) {
    Seq(ThriftEnumSchemaFile.exportString(tgtPkg, e))
  } else {
    Seq(ThriftStringEnumFile.exportString(srcPkg, tgtPkg, e, exportModelRoot), ThriftEnumSchemaFile.exportString(tgtPkg, e))
  })

  lazy val intEnums = decls.filter(_.isInstanceOf[IntegerEnum]).map(_.asInstanceOf[IntegerEnum]).map(ThriftIntegerEnum.apply)
  lazy val intEnumNames = intEnums.map(_.name)
  lazy val intEnumString = intEnums.map(e => s"  ${e.name} (${e.fields.size} values)").mkString("\n")
  lazy val intEnumFiles = intEnums.flatMap(e => if (flags("simple")) {
    Seq(ThriftIntEnumFile.exportInt(srcPkg, tgtPkg, e, exportModelRoot, flags))
  } else if (flags("extras")) {
    Seq(ThriftEnumSchemaFile.exportInt(tgtPkg, e))
  } else {
    Seq(ThriftIntEnumFile.exportInt(srcPkg, tgtPkg, e, exportModelRoot, flags), ThriftEnumSchemaFile.exportInt(tgtPkg, e))
  })

  lazy val enumDefaults = (stringEnums.map(e => e.name -> ExportHelper.toClassName(e.values.head)) ++
    intEnums.map(e => e.name -> ExportHelper.toClassName(e.fields.head._1))).toMap

  lazy val metadata = ThriftMetadata(typedefs, enumDefaults, pkgMap)

  lazy val structs = decls.filter(_.isInstanceOf[Struct]).map(_.asInstanceOf[Struct]).map(ThriftStruct.apply)
  lazy val structNames = structs.map(_.name)
  lazy val structString = structs.map(struct => s"  ${struct.name} (${struct.fields.size} fields)").mkString("\n")
  lazy val structFiles = structs.flatMap(struct => if (flags("simple")) {
    Seq(ThriftModelFile.export(srcPkg, tgtPkg, struct, metadata, exportModelRoot, overrides))
  } else if (flags("extras")) {
    Seq(ThriftModelSchemaFile.export(srcPkg, tgtPkg, struct, metadata))
  } else {
    Seq(ThriftModelFile.export(srcPkg, tgtPkg, struct, metadata, exportModelRoot, overrides), ThriftModelSchemaFile.export(srcPkg, tgtPkg, struct, metadata))
  })

  lazy val services = decls.filter(_.isInstanceOf[Service]).map(_.asInstanceOf[Service]).map(ThriftService.apply)
  lazy val serviceNames = services.map(_.name)
  lazy val serviceString = services.map(struct => s"  ${struct.name} (${struct.methods.size} methods)").mkString("\n")
  lazy val serviceFiles = services.flatMap(service => if (flags("simple")) {
    Seq(ThriftServiceFile.export(srcPkg, tgtPkg, service, metadata, exportModelRoot, overrides))
  } else if (flags("extras")) {
    Seq(
      ThriftTwirlServiceFile.export(tgtPkg, service, metadata),
      ThriftControllerFile.export(tgtPkg, service, metadata),
      ThriftRoutesFile.export(service),
      ThriftServiceSchemaFile.export(srcPkg, tgtPkg, service, metadata)
    )
  } else {
    Seq(
      ThriftServiceFile.export(srcPkg, tgtPkg, service, metadata, exportModelRoot, overrides),
      ThriftTwirlServiceFile.export(tgtPkg, service, metadata),
      ThriftControllerFile.export(tgtPkg, service, metadata),
      ThriftRoutesFile.export(service),
      ThriftServiceSchemaFile.export(srcPkg, tgtPkg, service, metadata)
    )
  })

  lazy val files = intEnumFiles ++ stringEnumFiles ++ structFiles ++ serviceFiles
  lazy val allFiles: Seq[OutputFile] = includes.flatMap(_.allFiles) ++ files

  lazy val summaryString = s"""[[[$filename]]]
    |Package: [${srcPkg.mkString(".")}]
    |Models:
    |$structString
    |Services:
    |$serviceString
  """.stripMargin.trim

  override lazy val toString = {
    val incSummary = if (includes.isEmpty) { "" } else { includes.map(_.summaryString).mkString("\n\n") + "\n\n" }
    incSummary + summaryString + s"\n\nFiles:" + files.map(file => "\n\n[" + file.filename + "]\n" + file.rendered).mkString
  }
}
