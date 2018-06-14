package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import sangria.ast._
import sangria.schema.{Type => Typ}

object GraphQLQueryHelper {
  def addFields(file: ScalaFile, typ: Option[Typ], selections: Seq[Selection], nameMap: Map[String, GraphQLQueryParseService.ClassName]) = {
    selections.foreach { s =>
      val param = s match {
        case Field(alias, name, _, _, sels, _, _, _) => s"${alias.getOrElse(name)}: ${typeForSelections(file, typ, sels, nameMap)}"
        case x => s"/* $x */"
      }
      val comma = if (selections.lastOption.contains(s)) { "" } else { "," }
      file.add(param + comma)
    }
  }

  private[this] def typeForSelections(file: ScalaFile, typ: Option[Typ], sels: Vector[Selection], nameMap: Map[String, GraphQLQueryParseService.ClassName]) = {
    val spreads = sels.flatMap {
      case x: FragmentSpread => Some(x)
      case _ => None
    }
    val fields = sels.flatMap {
      case _: FragmentSpread => None
      case x => Some(x)
    }
    spreads.toList match {
      case h :: Nil if fields.isEmpty =>
        val cn = nameMap.getOrElse(h.name, throw new IllegalStateException(s"Cannot find fragment definition for [${h.name}]."))
        file.addImport(cn.pkg.mkString("."), cn.cn)
        cn.cn
      case _ => "Json"
    }
  }
}
