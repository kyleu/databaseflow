package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.services.scalaexport.graphql.GraphQLQueryParseService.ClassName
import sangria.ast.{ListType, NamedType, NotNullType, Type}

object GraphQLInputTranslations {
  def scalaType(typ: Type): String = typ match {
    case NotNullType(x, _) => scalaType(x)
    case ListType(x, _) => s"Seq[${scalaType(x)}]"
    case NamedType(name, _) => name match {
      case "FilterInput" => "Filter"
      case "OrderByInput" => "OrderBy"
      case _ => name
    }
  }

  def scalaImport(providedPrefix: String, t: Type, nameMap: Map[String, ClassName]): Option[(String, String)] = t match {
    case NotNullType(x, _) => scalaImport(providedPrefix, x, nameMap)
    case ListType(x, _) => scalaImport(providedPrefix, x, nameMap)
    case _ => t.namedType.renderCompact match {
      case "UUID" => Some("java.util" -> "UUID")
      case "FilterInput" => Some((providedPrefix + "models.result.filter") -> "Filter")
      case "OrderByInput" => Some((providedPrefix + "models.result.orderBy") -> "OrderBy")
      case x => nameMap.get(x).map(x => x.pkg.mkString(".") -> x.cn)
    }
  }

  def defaultVal(typ: String) = typ match {
    case _ if typ.startsWith("Seq[") => " = Nil"
    case _ if typ.startsWith("Option[") => " = None"
    case _ => ""
  }
}
