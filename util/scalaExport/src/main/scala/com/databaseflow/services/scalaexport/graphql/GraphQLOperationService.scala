package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import sangria.ast._
import sangria.schema.{ObjectType, Schema, Type => Typ}

import scala.io.Source

object GraphQLOperationService {
  import GraphQLQueryParseService._

  def opFile(cfg: GraphQLExportConfig, n: String, d: OperationDefinition, nameMap: Map[String, ClassName], schema: Option[Schema[_, _]]) = {
    val cn = nameMap(n)
    val file = ScalaFile(cn.pkg, cn.cn, Some(""))
    file.addImport(cfg.rootPrefix + "util.JsonSerializers", "_")
    file.addImport(cfg.rootPrefix + "graphql", "GraphQLQuery")

    meaningfulComments(d.comments).foreach(c => file.add("// " + c))

    file.add(s"""object ${cn.cn} {""", 1)
    addVariables(cfg.rootPrefix, file, d.variables)
    val typ: Option[Typ] = if (d.operationType == OperationType.Query) {
      schema.map(_.query)
    } else if (d.operationType == OperationType.Mutation) {
      schema.map(_.mutation.get)
    } else {
      throw new IllegalStateException(d.operationType.toString)
    }
    addData(cfg.rootPrefix, cfg.modelPkg, typ, file, cn.pkg, d.selections, nameMap)
    file.add(s"""val query = new GraphQLQuery[Data]("${cn.cn}")""")
    //addContent(file, d)
    file.add("}", -1)
    Some(file)
  }

  private[this] def addVariables(rootPrefix: String, file: ScalaFile, variables: Seq[VariableDefinition]) = if (variables.nonEmpty) {
    variables.foreach(v => GraphQLInputTranslations.scalaImport(rootPrefix, v.tpe).foreach(x => file.addImport(x._1, x._2)))

    val args = variables.map { v =>
      val typ = GraphQLInputTranslations.scalaType(v.tpe)
      s"${v.name}: $typ${GraphQLInputTranslations.defaultVal(typ)}"
    }.mkString(", ")
    val varsDecl = variables.map(v => s""""${v.name}" -> ${v.name}.asJson""").mkString(", ")
    file.add(s"def variables($args) = {", 1)
    file.addImport("io.circe", "Json")
    file.add(s"Json.obj($varsDecl)")
    file.add("}", -1)
    file.add()
  }

  private[this] def addData(
    rootPrefix: String, modelPackage: String, typ: Option[Typ], file: ScalaFile, pkg: Seq[String], selections: Seq[Selection], nameMap: Map[String, ClassName]
  ) = {
    file.add(s"object Data {", 1)
    file.add(s"implicit val jsonDecoder: Decoder[Data] = deriveDecoder")
    file.add(s"}", -1)

    file.add(s"case class Data(", 2)
    GraphQLQueryHelper.addFields(rootPrefix, modelPackage, file, pkg, typ, selections, nameMap)
    file.add(s")", -2)
    file.add()
  }

  private[this] def addContent(file: ScalaFile, op: OperationDefinition) = {
    file.add("override val content = \"\"\"", 1)
    Source.fromString(op.renderPretty).getLines.foreach(l => file.add("|" + l))
    file.add("\"\"\".stripPrefix.trim", -1)
    file.add()
  }
}
