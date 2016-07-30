package services.codegen

import java.nio.charset.Charset

import models.codegen.{Capabilities, Engine}
import org.apache.commons.io.FileUtils

object CodeGenerator {
  val rdbmsEngineSourceDir = new java.io.File("./shared/src/main/scala/models/engine")
  val typesDir = new java.io.File(rdbmsEngineSourceDir, "types")
  val functionsDir = new java.io.File(rdbmsEngineSourceDir, "functions")

  def go() = {
    Engine.values.filter(_.enabled).map(CapabilitiesProvider.capabilitiesFor).foreach(writeCap)
  }

  def writeCap(cap: Capabilities) = {
    val typesOut = new java.io.File(typesDir, cap.engine.getClass.getSimpleName.stripSuffix("$") + "Types.scala")
    val typesTemplate = getTypesTemplate(cap)
    FileUtils.write(typesOut, typesTemplate, Charset.defaultCharset)

    val functionsOut = new java.io.File(functionsDir, cap.engine.getClass.getSimpleName.stripSuffix("$") + "Functions.scala")
    val functionsTemplate = getFunctionsTemplate(cap)
    FileUtils.write(functionsOut, functionsTemplate, Charset.defaultCharset)
  }

  private[this] def q(s: String) = "\"" + s + "\""

  def getTypesTemplate(cap: Capabilities) = {
    val ret = collection.mutable.ArrayBuffer.empty[String]
    implicit val eng = cap.engine
    ret += "/* Generated Code */"
    ret += "package models.engine.types"
    ret += ""
    ret += s"object ${eng.getClass.getSimpleName.stripSuffix("$")}Types extends TypeProvider {"
    ret += "  override val columnTypes = Seq("
    ret += cap.columnTypes.flatMap(_._2.map(x => s"    ${q(x)}")).mkString(",\n")
    ret += "  )"
    ret += "}"
    ret += ""

    ret.mkString("\n")
  }

  def getFunctionsTemplate(cap: Capabilities) = {
    val ret = collection.mutable.ArrayBuffer.empty[String]
    implicit val eng = cap.engine
    ret += "/* Generated Code */"
    ret += "// scalastyle:off"
    ret += "package models.engine.functions"
    ret += ""
    ret += s"object ${eng.getClass.getSimpleName.stripSuffix("$")}Functions extends FunctionProvider {"
    ret += "  override val functions = Seq("
    ret += cap.functions.map { f =>
      s"    ${q(f.name)}"
    }.mkString(",\n")
    ret += "  )"
    ret += "}"
    ret += "// scalastyle:on"
    ret += ""

    ret.mkString("\n")
  }
}
