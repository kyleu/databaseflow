package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import com.databaseflow.services.scalaexport.graphql.GraphQLQueryParseService.ClassName
import sangria.schema.{EnumType, ListType, OptionType, Type}

object GraphQLOutputTranslations {
  def scalaType(typ: Type, nameMap: Map[String, ClassName]): String = typ match {
    case OptionType(x) => s"Option[${scalaType(x, nameMap)}]"
    case ListType(x) => s"Seq[${scalaType(x, nameMap)}]"
    case _ => typ.namedType.name match {
      case "DateTime" | "Date" | "Time" => "Zoned" + typ.namedType.name
      case x => nameMap.get(x).map(_.cn).getOrElse(x)
    }
  }

  def scalaImport(cfg: GraphQLExportConfig, t: Type, nameMap: Map[String, ClassName]): Option[(String, String)] = t match {
    case OptionType(x) => scalaImport(cfg, x, nameMap)
    case ListType(x) => scalaImport(cfg, x, nameMap)
    case EnumType(n, _, _, _, _) => Some(nameMap.get(n).map(_.pkg.mkString(".")).getOrElse(cfg.pkgFor(n)) -> n)
    case _ => t.namedType.name match {
      case "DateTime" | "Date" | "Time" => Some("java.time" -> ("Zoned" + t.namedType.name))
      case "UUID" => Some("java.util" -> t.namedType.name)
      case "BigDecimal" => None
      case _ => None
    }
  }
}
