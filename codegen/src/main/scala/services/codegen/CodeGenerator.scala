package services.codegen

import models.codegen.{ Capabilities, Engine }
import org.apache.commons.io.FileUtils

object CodeGenerator {
  val rdbmsEngineSourceDir = new java.io.File("./shared/src/main/scala/models/engine/rdbms")

  def go() = {
    Engine.values.map(CapabilitiesProvider.capabilitiesFor).foreach(writeCap)
  }

  def writeCap(cap: Capabilities) = {
    val outFile = new java.io.File(rdbmsEngineSourceDir, cap.engine.getClass.getSimpleName.stripSuffix("$") + ".scala")
    println(outFile)

    val template = getTemplate(cap)
    println(template)

    FileUtils.write(outFile, template)
  }

  private[this] def q(s: String) = "\"" + s + "\""

  def getTemplate(cap: Capabilities) = {
    val ret = collection.mutable.ArrayBuffer.empty[String]
    implicit val eng = cap.engine
    ret += "/* Generated Code */"
    ret += "// scalastyle:off"
    ret += "package models.engine.rdbms"
    ret += ""
    ret += "import models.engine.DatabaseEngine"
    ret += ""
    ret += s"object ${eng.getClass.getSimpleName.stripSuffix("$")} extends DatabaseEngine("
    ret += s"  id = ${q(eng.id)},"
    ret += s"  name = ${q(eng.name)},"
    ret += s"  driverClass = ${q(eng.driverClass)},"
    ret += s"  exampleUrl = ${q(eng.exampleUrl)},"
    ret += ""
    ret += "  builtInFunctions = Seq("
    ret += cap.builtInFunctions.map { f =>
      s"    ${q(f.name)}"
    }.mkString(",\n")
    ret += "  ),"
    ret += ""
    ret += "  columnTypes = Seq("
    ret += cap.columnTypes.flatMap(_._2.map(x => s"    ${q(x)}")).mkString(",\n")
    ret += "  )"
    ret += ") {"
    ret += "  override val leftQuoteIdentifier = \"" + SqlProvider.leftQuoteIdentifier + "\""
    ret += "  override val rightQuoteIdentifier = \"" + SqlProvider.rightQuoteIdentifier + "\""
    if (PlanProvider.explainSupported) {
      ret += s"  override val explain = Some((sql: String) => { ${PlanProvider.explain} })"
    }
    if (PlanProvider.analyzeSupported) {
      ret += s"  override val analyze = Some((sql: String) => { ${PlanProvider.analyze} })"
    }
    ret += "}"
    ret += "// scalastyle:on"
    ret += ""

    ret.mkString("\n")
  }
}
