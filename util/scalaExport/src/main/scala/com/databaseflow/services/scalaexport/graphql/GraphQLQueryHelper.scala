package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.services.scalaexport.graphql.GraphQLQueryParseService.ClassName
import com.databaseflow.services.scalaexport.graphql.GraphQLOutputTranslations.{scalaImport, scalaType}
import sangria.ast._
import sangria.schema.{ObjectType, OutputType, ScalarType, Type => Typ}

object GraphQLQueryHelper {
  def addFields(
    providedPrefix: String, modelPackage: String, file: ScalaFile, pkg: Seq[String], typ: Option[Typ], selections: Seq[Selection], nameMap: Map[String, ClassName]
  ) = {
    selections.foreach { s =>
      val param = s match {
        case Field(alias, name, _, _, sels, _, _, _) =>
          val t = typeForSelections(providedPrefix, modelPackage, file, name, pkg, typ, sels, nameMap)
          s"${alias.getOrElse(name)}: $t"
        case x => s"/* $x */"
      }
      val comma = if (selections.lastOption.contains(s)) { "" } else { "," }
      file.add(param + comma)
    }
  }

  def monadsFor(fieldType: OutputType[_], cn: String): String = fieldType match {
    case sangria.schema.ListType(x) => s"Seq[${monadsFor(x, cn)}]"
    case sangria.schema.OptionType(x) => s"Option[${monadsFor(x, cn)}]"
    case _ => cn
  }

  private[this] def typeForSelections(
    providedPrefix: String, modelPackage: String, file: ScalaFile, name: String, pkg: Seq[String], typ: Option[Typ], sels: Vector[Selection], nameMap: Map[String, ClassName]
  ) = {
    val spreads = sels.flatMap {
      case x: FragmentSpread => Some(x)
      case _: InlineFragment => throw new IllegalStateException("I don't know what an InlineFragment is.")
      case _ => None
    }
    val fields = sels.flatMap {
      case x: Field => Some(x)
      case _ => None
    }
    spreads.toList match {
      case h :: Nil if fields.isEmpty =>
        val cn = nameMap.getOrElse(h.name, throw new IllegalStateException(s"Cannot find fragment definition for [${h.name}]."))
        if (cn.pkg.toSeq != pkg) {
          file.addImport(cn.pkg.mkString("."), cn.cn)
        }
        typ match {
          case Some(t) => t match {
            case o: ObjectType[_, _] => o.fields.find(_.name == name) match {
              case Some(f) => monadsFor(f.fieldType, cn.cn)
              case None => throw new IllegalStateException(
                s"Cannot find field [${h.name}] on type [${t.namedType.name}] from [${o.fields.map(_.name).mkString(", ")}]."
              )
            }
            case x => throw new IllegalStateException(" ::: " + x)
          }
          case None => cn.cn
        }
      case Nil if fields.isEmpty => typ match {
        case Some(t) => t match {
          case o: ObjectType[_, _] =>
            val fieldType = o.fields.find(_.name == name).getOrElse {
              throw new IllegalStateException(s"Cannot find field [$name] on type [${t.namedType.name}] from [${o.fields.map(_.name).mkString(", ")}].")
            }.fieldType
            scalaImport(providedPrefix, modelPackage, fieldType).foreach(x => file.addImport(x._1, x._2))
            scalaType(fieldType)
          case _ => s"Json /* $t */"
        }
        case None =>
          file.addImport("io.circe", "Json")
          "Json /* TODO */"
      }
      case _ =>
        file.addImport("io.circe", "Json")
        "Json"
    }
  }
}
