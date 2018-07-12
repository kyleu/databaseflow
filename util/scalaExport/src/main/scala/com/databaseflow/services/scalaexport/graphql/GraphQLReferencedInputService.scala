package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import sangria.schema.{InputField, InputObjectType, InputType, ListInputType, OptionInputType, Schema}

object GraphQLReferencedInputService {
  import GraphQLQueryParseService._

  private[this] def typeForSelections(file: ScalaFile, typ: InputType[_], nameMap: Map[String, ClassName]): String = {
    typ match {
      case ListInputType(t) => s"Seq[${typeForSelections(file, t, nameMap)}]"
      case OptionInputType(t) => s"Option[${typeForSelections(file, t, nameMap)}]"
      case n => n.namedType.name match {
        case "UUID" =>
          file.addImport("java.util", "UUID")
          "UUID"
        case "DateTime" =>
          file.addImport("java.time", "ZonedDateTime")
          "ZonedDateTime"
        case x => x
      }
    }
  }

  private[this] def addInputFields(
    cfg: GraphQLExportConfig, file: ScalaFile, pkg: Array[String], fields: List[InputField[_]], nameMap: Map[String, ClassName]
  ) = {
    fields.foreach { f =>
      val t = typeForSelections(file, f.fieldType, nameMap)
      val param = s"${f.name}: $t"
      val comma = if (fields.lastOption.contains(f)) { "" } else { "," }
      file.add(param + comma)
    }
  }

  def inputFile(cfg: GraphQLExportConfig, d: InputObjectType[_], nameMap: Map[String, ClassName], schema: Schema[_, _]) = {
    if (nameMap.get(d.name).exists(_.provided)) {
      None
    } else {
      val cn = nameMap(d.name)
      val file = ScalaFile(cn.pkg, cn.cn, Some(""))
      file.addImport(cfg.providedPrefix + "util.JsonSerializers", "_")

      file.add(s"object ${cn.cn} {", 1)
      file.add(s"implicit val jsonDecoder: Decoder[${cn.cn}] = deriveDecoder")
      file.add(s"implicit val jsonEncoder: Encoder[${cn.cn}] = deriveEncoder")
      file.add("}", -1)
      file.add()
      file.add(s"case class ${cn.cn}(", 2)
      addInputFields(cfg, file, cn.pkg, d.fields, nameMap)
      file.add(")", -2)

      Some(file)
    }
  }
}
