package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import sangria.schema.{EnumType, ListType, OptionType, Type}

object GraphQLOutputTranslations {
  def scalaType(typ: Type): String = typ match {
    case OptionType(x) => s"Option[${scalaType(x)}]"
    case ListType(x) => s"Seq[${scalaType(x)}]"
    case _ => typ.namedType.name match {
      case "DateTime" | "Date" | "Time" => "Local" + typ.namedType.name
      case x => x
    }
  }

  def scalaImport(cfg: GraphQLExportConfig, t: Type): Option[(String, String)] = t match {
    case OptionType(x) => scalaImport(cfg, x)
    case ListType(x) => scalaImport(cfg, x)
    case EnumType(n, _, _, _, _) => Some(cfg.pkgFor(n) -> n)
    case _ => t.namedType.name match {
      case "DateTime" | "Date" | "Time" => Some("java.time" -> ("Local" + t.namedType.name))
      case "UUID" => Some("java.util" -> t.namedType.name)
      case "BigDecimal" => None
      case _ => None
    }
  }
}
