package com.databaseflow.services.scalaexport.graphql

import better.files._
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import sangria.ast._
import sangria.parser.QueryParser
import com.databaseflow.services.scalaexport.ExportHelper
import sangria.schema.{EnumType, Schema}

object GraphQLQueryParseService {
  val classPrefix = "Class: "
  val providedPrefix = "Provided: "
  case class ClassName(pkg: Array[String], cn: String, provided: Boolean)

  def meaningfulComments(comments: Seq[Comment]) = comments.map(_.text.trim).filterNot { c =>
    c.startsWith("#") || c.startsWith(classPrefix) || c.startsWith(providedPrefix)
  }
}

class GraphQLQueryParseService(cfg: GraphQLExportConfig, schema: Option[Schema[_, _]]) {
  import GraphQLQueryParseService._

  def export() = {
    val input = cfg.input.toFile
    val queryFiles = if (input.isDirectory) {
      input.children.filter(_.isRegularFile).filter(_.name.endsWith(".graphql")).toSeq
    } else if (input.isRegularFile) {
      Seq(input)
    } else {
      throw new IllegalStateException(s"Cannot load data input directory [${cfg.input}].")
    }

    val doc = queryFiles.foldLeft(Document.emptyStub)((d, f) => d.merge(QueryParser.parse(f.contentAsString).get))

    val enumTypes = schema.map(s => s.allTypes.values.flatMap {
      case EnumType(name, _, values, _, _) => Some(name -> values)
      case _ => None
    }).getOrElse(Nil).toSeq

    val nameMap = {
      val enumStuff = calcNames(enumTypes.map(_._1 -> Nil), Seq("graphql", "enums"))
      val fragStuff = calcNames(doc.fragments.mapValues(_.comments).toSeq, Seq("graphql", "fragments"))
      val inputStuff = calcNames(doc.definitions.collect { case x: InputObjectTypeDefinition => x }.map(x => x.name -> x.comments), Seq("graphql", "inputs"))
      val opStuff = calcNames(doc.operations.map(o => o._1.get -> o._2.comments).toSeq, Seq("graphql", "queries"))
      enumStuff ++ fragStuff ++ inputStuff ++ opStuff
    }

    val enumFiles = enumTypes.flatMap(f => GraphQLEnumService.enumFile(cfg, f._1, f._2, nameMap))

    val fragmentFiles = doc.fragments.flatMap(f => GraphQLFragmentService.fragmentFile(cfg, f._1, f._2, nameMap, schema))

    val inputFiles = doc.definitions.flatMap {
      case x: InputObjectTypeDefinition => GraphQLInputService.inputFile(cfg, x, nameMap, schema)
      case _ => None
    }

    val opFiles = doc.operations.flatMap(f => GraphQLOperationService.opFile(cfg, f._1.get, f._2, nameMap, schema))

    val outFiles = (enumFiles ++ fragmentFiles ++ inputFiles ++ opFiles).toSeq

    val output = cfg.output.toFile
    if (!output.isDirectory) {
      throw new IllegalStateException(s"Cannot load data output directory [${cfg.output}].")
    }

    outFiles.foreach { f =>
      val out = output / f.path.stripPrefix("/")
      out.createIfNotExists(createParents = true)
      out.write(f.rendered)
    }
  }

  private[this] def calcNames(types: Seq[(String, Seq[Comment])], pkgSuffix: Seq[String] = Nil) = types.map { t =>
    t._2.find(c => c.text.trim.startsWith(classPrefix) || c.text.trim.startsWith(providedPrefix)).map { x =>
      val (pkg, name) = x.text.trim.stripPrefix(classPrefix).stripPrefix(providedPrefix).trim match {
        case n if n.contains('.') => n.substring(0, n.lastIndexOf('.')).split('.').filter(_.nonEmpty) -> n.substring(n.lastIndexOf('.') + 1)
        case n => (cfg.pkgSeq ++ pkgSuffix) -> n
      }
      (t._1, ClassName(pkg, name, x.text.trim.startsWith(providedPrefix)))
    }.getOrElse(t._1 -> ClassName(cfg.pkgSeq ++ pkgSuffix, ExportHelper.toClassName(t._1), provided = false))
  }.toMap
}
