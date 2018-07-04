package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import com.databaseflow.services.scalaexport.ExportHelper
import com.databaseflow.services.scalaexport.graphql.GraphQLQueryParseService.ClassName
import sangria.ast._
import sangria.schema.{ObjectType, Schema, Type => Typ}

object GraphQLObjectHelper {
  def objectFor(
    cfg: GraphQLExportConfig, file: ScalaFile, cn: ClassName, typ: Typ, sels: Vector[Selection],
    nameMap: Map[String, ClassName], schema: Schema[_, _], incEncoder: Boolean = false
  ): Unit = {
    childDefinitions(cfg, file, cn, typ, sels, nameMap, schema, incEncoder)

    file.add()
    file.add(s"object ${cn.cn} {", 1)

    file.add(s"implicit val jsonDecoder: Decoder[${cn.cn}] = deriveDecoder")
    if (incEncoder) {
      file.add(s"implicit val jsonEncoder: Encoder[${cn.cn}] = deriveEncoder")
    }
    file.add("}", -1)
    file.add(s"case class ${cn.cn}(", 2)
    GraphQLFieldHelper.addFields(cfg, file, cn.pkg, typ, sels, nameMap)
    file.add(")", -2)
  }

  private[this] def childDefinitions(
    cfg: GraphQLExportConfig, file: ScalaFile, cn: ClassName, typ: Typ, sels: Vector[Selection],
    nameMap: Map[String, ClassName], schema: Schema[_, _], incEncoder: Boolean
  ): Unit = {
    val (spreads, fields) = GraphQLFieldHelper.distribute(sels)
    spreads match {
      case h :: Nil if fields.isEmpty => // noop
      case h :: Nil => throw new IllegalStateException("Fragment spread cannot be used with field listing.")
      case Nil if fields.isEmpty => // noop
      case Nil => childDef(cfg, file, cn.pkg, typ, fields, nameMap, schema, incEncoder)
      case _ => throw new IllegalStateException("Multiple fragment spreads.")
    }
  }

  private[this] def childDef(
    cfg: GraphQLExportConfig, file: ScalaFile, pkg: Array[String], typ: Typ, fields: List[Field],
    nameMap: Map[String, ClassName], schema: Schema[_, _], incEncoder: Boolean
  ): Unit = typ match {
    case sangria.schema.ListType(t) => childDef(cfg, file, pkg, t, fields, nameMap, schema, incEncoder)
    case ObjectType(name, _, fieldsFn, _, _, _, _) =>
      val tgtFields = fieldsFn()
      fields.foreach {
        case f if f.selections.isEmpty => // noop
        case f =>
          val newCn = ClassName(pkg, ExportHelper.toClassName(f.name) + (if (f.selections.size == 1) { "Wrapper" } else { "Child" }), provided = false)
          val newTyp = tgtFields.find(_.name == f.name).getOrElse {
            throw new IllegalStateException(s"Cannot load field [${f.name}] from available [${tgtFields.map(_.name).mkString(", ")}].")
          }.fieldType
          val (spreads, fields) = GraphQLFieldHelper.distribute(f.selections)
          if (spreads.isEmpty && fields.nonEmpty) {
            objectFor(cfg, file, newCn, newTyp.namedType, f.selections, nameMap, schema, incEncoder)
          }
      }
    case _ => throw new IllegalStateException(s"Unhandled typ [$typ].")
  }
}
