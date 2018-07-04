package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import com.databaseflow.services.scalaexport.ExportHelper
import com.databaseflow.services.scalaexport.graphql.GraphQLOutputTranslations.{scalaImport, scalaType}
import com.databaseflow.services.scalaexport.graphql.GraphQLQueryParseService.ClassName
import sangria.ast._
import sangria.schema.{ObjectType, OutputType, Type => Typ}

object GraphQLFieldHelper {
  def addFields(
    cfg: GraphQLExportConfig, file: ScalaFile, pkg: Seq[String], typ: Typ, selections: Seq[Selection], nameMap: Map[String, ClassName]
  ) = {
    selections.foreach { s =>
      val param = s match {
        case Field(alias, name, _, _, sels, _, _, _) =>
          val t = typeForSelections(cfg, file, name, pkg, typ, sels, nameMap)
          s"${alias.getOrElse(name)}: $t"
        case _ => throw new IllegalStateException(s"Unhandled selection [$s].")
      }
      val comma = if (selections.lastOption.contains(s)) { "" } else { "," }
      file.add(param + comma)
    }
  }

  private[this] def monadsFor(fieldType: Typ, cn: String): String = fieldType match {
    case sangria.schema.ListType(x) => s"Seq[${monadsFor(x, cn)}]"
    case sangria.schema.OptionType(x) => s"Option[${monadsFor(x, cn)}]"
    case _ => cn
  }

  private[this] def extractFieldType(typ: Typ, name: String, typName: String, cn: ClassName) = typ match {
    case o: ObjectType[_, _] => o.fields.find(_.name == name) match {
      case Some(f) => monadsFor(f.fieldType, cn.cn)
      case None => throw new IllegalStateException(
        s"Cannot find field [$typName] on type [${typ.namedType.name}] from [${o.fields.map(_.name).mkString(", ")}]."
      )
    }
    case x => throw new IllegalStateException(" ::: " + x)
  }

  private[this] def typeForSelections(
    cfg: GraphQLExportConfig, file: ScalaFile, name: String, pkg: Seq[String], typ: Typ, sels: Vector[Selection], nameMap: Map[String, ClassName]
  ) = {
    val (spreads, fields) = distribute(sels)
    spreads match {
      case h :: Nil if fields.isEmpty =>
        val cn = nameMap.getOrElse(h.name, throw new IllegalStateException(s"Cannot find fragment definition for [${h.name}]."))
        if (cn.pkg.toSeq != pkg) {
          file.addImport(cn.pkg.mkString("."), cn.cn)
        }
        extractFieldType(typ, name, h.name, cn)
      case h :: Nil => throw new IllegalStateException("Fragment spread cannot be used with field listing.")
      case Nil if fields.isEmpty => typ match {
        case o: ObjectType[_, _] =>
          val fieldType = o.fields.find(_.name == name).getOrElse {
            throw new IllegalStateException(s"Cannot find field [$name] on type [${typ.namedType.name}] from [${o.fields.map(_.name).mkString(", ")}].")
          }.fieldType
          scalaImport(cfg, fieldType, nameMap).foreach(x => file.addImport(x._1, x._2))
          scalaType(fieldType, nameMap)
        case _ => s"Json /* Unknown type (${typ.getClass.getName}}) */"
      }
      case Nil =>
        val n = fields match {
          case h :: Nil => ExportHelper.toClassName(name) + "Wrapper"
          case _ => ExportHelper.toClassName(name) + "Child"
        }
        val cn = ClassName(pkg = pkg.toArray, cn = n, provided = false)
        extractFieldType(typ, name, "TODO", cn)
      case _ => throw new IllegalStateException("Multiple fragment spreads.")
    }
  }

  def distribute(sels: Vector[Selection]) = {
    val spreads = sels.flatMap {
      case x: FragmentSpread => Some(x)
      case _: InlineFragment => throw new IllegalStateException("I don't know what an InlineFragment is.")
      case _ => None
    }
    val fields = sels.flatMap {
      case x: Field => Some(x)
      case _ => None
    }
    spreads.toList -> fields.toList
  }
}
