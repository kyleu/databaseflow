package com.databaseflow.services.scalaexport.graphql

import better.files._
import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import sangria.ast._
import sangria.parser.QueryParser
import com.databaseflow.services.scalaexport.ExportHelper

class GraphQLQueryParseService(cfg: GraphQLExportConfig) {
  private[this] val classPrefix = "Class: "
  private[this] val providedPrefix = "Provided: "

  private[this] case class ClassName(pkg: Array[String], cn: String, provided: Boolean)

  def export() = {
    val input = cfg.input.toFile
    if (!input.isDirectory) {
      throw new IllegalStateException(s"Cannot load data input directory [${cfg.input}].")
    }
    val queryFiles = input.children.filter(_.isRegularFile).filter(_.name.endsWith(".graphql"))
    val doc = queryFiles.foldLeft(Document.emptyStub)((d, f) => d.merge(QueryParser.parse(f.contentAsString).get))

    val nameMap = {
      val fragStuff = calcNames(doc.fragments.mapValues(_.comments).toSeq, Seq("graphql", "fragments"))
      val opStuff = calcNames(doc.operations.map(o => o._1.get -> o._2.comments).toSeq, Seq("graphql", "queries"))
      fragStuff ++ opStuff
    }

    val outFiles = filesFor(doc, nameMap)

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

  private[this] def filesFor(doc: Document, nameMap: Map[String, ClassName]) = {
    val fragmentFiles = doc.fragments.flatMap(f => fragmentFile(f._1, f._2, nameMap(f._1)))
    val opFiles = doc.operations.flatMap(f => opFile(f._1.get, f._2, nameMap(f._1.get)))
    (fragmentFiles ++ opFiles).toSeq
  }

  private[this] def fragmentFile(n: String, d: FragmentDefinition, cn: ClassName) = if (cn.provided) {
    None
  } else {
    val file = ScalaFile(cn.pkg, cn.cn, Some(""))
    file.addImport(cfg.rootPrefix + "util.JsonSerializers", "_")
    file.addImport("io.circe", "Json")

    d.comments.filterNot { c =>
      c.text.startsWith("#") || c.text.trim.startsWith(classPrefix) || c.text.trim.startsWith(providedPrefix)
    }.foreach(c => file.add("// " + c.text))

    file.add(s"object ${cn.cn} {", 1)
    file.add("}", -1)

    file.add(s"case class ${cn.cn}(", 1)
    GraphQLQueryHelper.addFields(file, d.selections)
    file.add(")", -1)

    Some(file)
  }

  private[this] def opFile(n: String, d: OperationDefinition, cn: ClassName) = {
    val file = ScalaFile(cn.pkg, cn.cn, Some(""))
    file.addImport(cfg.rootPrefix + "util.JsonSerializers", "_")
    file.addImport("io.circe", "Json")

    d.comments.filterNot { c =>
      c.text.startsWith("#") || c.text.trim.startsWith(classPrefix) || c.text.trim.startsWith(providedPrefix)
    }.foreach(c => file.add("// " + c.text))

    file.add(s"object ${cn.cn} {", 1)
    //GraphQLQueryHelper.addQuery(file, opDoc._2)
    GraphQLQueryHelper.addVariables(cfg.rootPrefix, file, d.variables)
    GraphQLQueryHelper.addData(file, d.selections)
    file.add("}", -1)
    Some(file)
  }
}
