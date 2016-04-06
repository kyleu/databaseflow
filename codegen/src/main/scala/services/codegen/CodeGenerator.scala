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
    val engName = eng.getClass.getSimpleName.stripSuffix("$")
    ret += "/* Generated Code */"
    ret += "// scalastyle:off"
    ret += "package models.engine.rdbms"
    ret += ""
    ret += "import models.engine.DatabaseEngine"
    ret += ""
    ret += s"object $engName extends DatabaseEngine("
    ret += s"  id = ${q(eng.id)},"
    ret += s"  name = ${q(engName)},"
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
    ret += "  override val varchar = \"" + SqlProvider.varchar + "\""
    ret += "  override val quoteIdentifier = \"" + SqlProvider.quoteIdentifier + "\""
    ret += s"  override val explainSupported = ${PlanProvider.explainSupported}"
    if (PlanProvider.explainSupported) {
      ret += s"  override def explain(sql: String) = ${PlanProvider.explain}"
    }
    ret += s"  override val analyzeSupported = ${PlanProvider.analyzeSupported}"
    if (PlanProvider.analyzeSupported) {
      ret += s"  override def analyze(sql: String) = ${PlanProvider.analyze}"
    }
    ret += s"  override val showCreateSupported = ${SqlProvider.showCreateSupported}"
    if (SqlProvider.showCreateSupported) {
      ret += s"  override def showCreateTable(tableName: String) = ${SqlProvider.showCreateTable}"
      ret += s"  override def showCreateView(viewName: String) = ${SqlProvider.showCreateView}"
    }
    ret += "}"
    ret += "// scalastyle:on"
    ret += ""

    ret.mkString("\n")
  }
}
