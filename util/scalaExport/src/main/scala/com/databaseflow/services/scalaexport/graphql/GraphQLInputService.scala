package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import sangria.ast._
import sangria.schema.Schema

object GraphQLInputService {
  import GraphQLQueryParseService._

  private[this] def typeForSelections(file: ScalaFile, typ: Type, nameMap: Map[String, ClassName], enums: Seq[String]): String = {
    typ match {
      case ListType(t, _) => s"Seq[${typeForSelections(file, t, nameMap, enums)}]"
      case NotNullType(t, _) => typeForSelections(file, t, nameMap, enums) match {
        case x if x.startsWith("Option") => x.stripPrefix("Option[").stripSuffix("]")
        case x => x
      }
      case NamedType(n, _) => s"Option[" + (n match {
        case x if enums.contains(x) =>
          val ecn = nameMap(x)
          file.addImport(ecn.pkg.mkString("."), ecn.cn)
          ecn.cn
        case "UUID" =>
          file.addImport("java.util", "UUID")
          "UUID"
        case "DateTime" =>
          file.addImport("java.time", "ZonedDateTime")
          "ZonedDateTime"
        case x => x
      }) + "]"
      case x => throw new IllegalStateException(s"Unhandled input type [$x].")
    }
  }

  private[this] def addInputFields(
    cfg: GraphQLExportConfig, file: ScalaFile, pkg: Array[String], fields: Vector[InputValueDefinition], nameMap: Map[String, ClassName], enums: Seq[String]
  ) = {
    fields.foreach { f =>
      val t = typeForSelections(file, f.valueType, nameMap, enums)
      val param = s"${f.name}: $t"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }

  def inputFile(cfg: GraphQLExportConfig, d: InputObjectTypeDefinition, nameMap: Map[String, ClassName], schema: Schema[_, _], enums: Seq[String]) = {
    if (nameMap.get(d.name).exists(_.provided)) {
      None
    } else {
      val cn = nameMap(d.name)
      val file = ScalaFile(cn.pkg, cn.cn, Some(""))
      file.addImport(cfg.providedPrefix + "util.JsonSerializers", "_")

      meaningfulComments(d.comments).foreach(c => file.add("// " + c))

      file.add(s"object ${cn.cn} {", 1)
      file.add(s"implicit val jsonDecoder: Decoder[${cn.cn}] = deriveDecoder")
      file.add(s"implicit val jsonEncoder: Encoder[${cn.cn}] = deriveEncoder")
      file.add("}", -1)
      file.add()
      file.add(s"case class ${cn.cn}(", 2)
      addInputFields(cfg, file, cn.pkg, d.fields, nameMap, enums)
      file.add(")", -2)

      Some(file)
    }
  }
}
