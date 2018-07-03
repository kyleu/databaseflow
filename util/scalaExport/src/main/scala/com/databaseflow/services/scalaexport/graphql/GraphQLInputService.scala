package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import sangria.ast._
import sangria.schema.Schema

object GraphQLInputService {
  import GraphQLQueryParseService._

  private[this] def typeForSelections(file: ScalaFile, typ: Type, nameMap: Map[String, ClassName]): String = {
    typ match {
      case ListType(t, _) => s"Seq[${typeForSelections(file, t, nameMap)}]"
      case NotNullType(t, _) => typeForSelections(file, t, nameMap).stripPrefix("Option[").stripSuffix("]")
      case NamedType(n, _) => s"Option[" + (n match {
        case "?" => "???"
        case "UUID" =>
          file.addImport("java.util", "UUID")
          "UUID"
        case x => throw new IllegalStateException(s"Unhandled input field type name [$x].")
      }) + "]"
      case x => throw new IllegalStateException(s"Unhandled input type [$x].")
    }
  }

  private[this] def addInputFields(
    cfg: GraphQLExportConfig, file: ScalaFile, pkg: Array[String], fields: Vector[InputValueDefinition], nameMap: Map[String, ClassName]
  ) = {
    fields.foreach { f =>
      val t = typeForSelections(file, f.valueType, nameMap)
      val param = s"${f.name}: $t"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }

  def inputFile(cfg: GraphQLExportConfig, d: InputObjectTypeDefinition, nameMap: Map[String, ClassName], schema: Option[Schema[_, _]]) = {
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

      file.add(s"case class ${cn.cn}(", 2)
      addInputFields(cfg, file, cn.pkg, d.fields, nameMap)
      file.add(")", -2)

      Some(file)
    }
  }
}
