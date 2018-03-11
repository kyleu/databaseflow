package services.scalaexport.file.thrift

import com.facebook.swift.parser.model.ThriftType
import models.scalaexport.ScalaFile
import services.scalaexport.ExportHelper

object ThriftSchemaHelper {
  private[this] def countSubstring(str: String, sub: String): Int = str.sliding(sub.length).count(_ == sub)

  case class ReplacedField(name: String, t: String, pkg: Seq[String], req: Boolean = true) {
    lazy val fieldType: String = t match {
      case _ if !req => s"OptionType(${ReplacedField(name, t, pkg).fieldType})"
      case "Long" => "LongType"
      case "String" => "StringType"
      case x if x.startsWith("Seq[") => s"ListType(${ReplacedField(name, t.drop(4).dropRight(1), pkg).fieldType})"
      case x if x.startsWith("Set[") => s"ListType(${ReplacedField(name, t.drop(4).dropRight(1), pkg).fieldType})"
      case x if x.startsWith("Map[") => s"StringType"
      case x => ExportHelper.toIdentifier(x) + "Type"
    }

    lazy val maps: String = t match {
      case _ if !req => s".map(some => some${ReplacedField(name, t, pkg).maps})"
      case _ if t.startsWith("Seq[") => s".map(el => el${ReplacedField(name, t.drop(4).dropRight(1), pkg).maps})"
      case _ if t.startsWith("Set[") => s".map(el => el${ReplacedField(name, t.drop(4).dropRight(1), pkg).maps}).toSeq"
      case x if x.startsWith("Map[") => s".toString"
      case _ => ""
    }

    lazy val fullFieldDecl: String = {
      s"""ReplaceField("$name", Field("$name", $fieldType, resolve = _.value.$name$maps))"""
    }
  }

  def getReplaceFields(
    pkg: Seq[String],
    types: Seq[(String, Boolean, ThriftType)],
    typedefs: Map[String, String],
    pkgMap: Map[String, Seq[String]]
  ): Seq[ReplacedField] = types.flatMap {
    case (n, r, t) => ThriftFileHelper.columnTypeFor(t, typedefs, pkgMap) match {
      case (x, p) if x.contains("Set[") || x.contains("Seq[") || x.contains("Map[") =>
        Some(ReplacedField(name = n, t = x, pkg = if (p.isEmpty) { pkg } else { p }, req = r))
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

  def addImports(
    pkg: Seq[String],
    types: Seq[ThriftType],
    typedefs: Map[String, String],
    pkgMap: Map[String, Seq[String]],
    file: ScalaFile
  ) = types.foreach { t =>
    val colType = ThriftFileHelper.columnTypeFor(t, typedefs, pkgMap)
    getImportType(colType._1).foreach { impType =>
      val impPkg = if (colType._2.isEmpty) {
        (pkg :+ "graphql").mkString(".")
      } else {
        (colType._2 :+ "graphql").mkString(".")
      }
      file.addImport(s"$impPkg.${impType}Schema", s"${ExportHelper.toIdentifier(impType)}Type")
    }
  }
}
