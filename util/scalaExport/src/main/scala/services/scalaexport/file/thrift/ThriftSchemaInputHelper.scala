package services.scalaexport.file.thrift

import com.facebook.swift.parser.model.ThriftType
import models.scalaexport.ScalaFile
import models.scalaexport.thrift.ThriftMetadata
import services.scalaexport.ExportHelper

object ThriftSchemaInputHelper {
  private[this] def graphQlInputTypeFor(t: String, enums: Map[String, String], req: Boolean = true): String = t match {
    case _ if !req => s"OptionInputType(${graphQlInputTypeFor(t, enums)})"
    case "Long" => "LongType"
    case "Double" => "FloatType"
    case "Float" => "FloatType"
    case "Int" => "IntType"
    case "String" => "StringType"
    case "Boolean" => "BooleanType"
    case "Unit" => "BooleanType"
    case x if x.startsWith("Seq[") => s"ListInputType(${graphQlInputTypeFor(t.drop(4).dropRight(1), enums)})"
    case x if x.startsWith("Set[") => s"ListInputType(${graphQlInputTypeFor(t.drop(4).dropRight(1), enums)})"
    case x if x.startsWith("Map[") => s"StringType"
    case x if enums.contains(x) => ExportHelper.toIdentifier(x) + "Type"
    case x => ExportHelper.toIdentifier(x) + "InputType"
  }

  case class ReplacedInputField(name: String, t: String, pkg: Seq[String], enums: Map[String, String], req: Boolean = true) {
    lazy val fullFieldDecl = s"""ReplaceInputField(fieldName = "$name", field = InputField(name = "$name", fieldType = ${graphQlInputTypeFor(t, enums, req)}))"""
  }

  def getReplaceInputFields(pkg: Seq[String], types: Seq[(String, Boolean, ThriftType)], metadata: ThriftMetadata): Seq[ReplacedInputField] = types.flatMap {
    case (n, r, t) => ThriftFileHelper.columnTypeFor(t, metadata) match {
      case (x, p) if x.contains("Set[") || x.contains("Seq[") || x.contains("Map[") || (!r) =>
        Some(ReplacedInputField(name = n, t = x, pkg = if (p.isEmpty) { pkg } else { p }, metadata.enums, req = r))
      case _ => None
    }
  }

  def getImportType(t: String): Option[String] = t match {
    case "Unit" | "Boolean" | "String" | "Int" | "Long" | "Double" => None
    case x if x.startsWith("Map[") => getImportType(ThriftFieldHelper.mapKeyValFor(x)._2)
    case x if x.startsWith("Seq[") => getImportType(t.drop(4).dropRight(1))
    case x if x.startsWith("Set[") => getImportType(t.drop(4).dropRight(1))
    case x => Some(x)
  }

  def addInputImports(pkg: Seq[String], types: Seq[ThriftType], metadata: ThriftMetadata, file: ScalaFile) = {
    types.foreach { t =>
      val colType = ThriftFileHelper.columnTypeFor(t, metadata)
      getImportType(colType._1).foreach { impType =>
        val impPkg = if (colType._2.isEmpty) { (pkg :+ "graphql").mkString(".") } else { (colType._2 :+ "graphql").mkString(".") }
        if (!metadata.enums.contains(impType)) {
          file.addImport(s"$impPkg.${impType}Schema", s"${ExportHelper.toIdentifier(impType)}InputType")
        }
      }
    }
  }
}
