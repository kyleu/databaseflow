package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import sangria.ast._

import scala.io.Source

object GraphQLOperationService {
  import GraphQLQueryParseService._

  def opFile(cfg: GraphQLExportConfig, n: String, d: OperationDefinition, nameMap: Map[String, ClassName]) = {
    val cn = nameMap(n)
    val file = ScalaFile(cn.pkg, cn.cn, Some(""))
    file.addImport(cfg.rootPrefix + "util.JsonSerializers", "_")
    file.addImport(cfg.rootPrefix + "graphql", "GraphQLQuery")
    file.addImport("io.circe", "Json")

    meaningfulComments(d.comments).foreach(c => file.add("// " + c))

    file.add(s"""object ${cn.cn} {""", 1)
    addVariables(cfg.rootPrefix, file, d.variables)
    addData(file, d.selections, nameMap)
    file.add(s"""val query = new GraphQLQuery[Data]("${cn.cn}")""")
    //addContent(file, d)
    file.add("}", -1)
    Some(file)
  }

  private[this] def addVariables(rootPrefix: String, file: ScalaFile, variables: Seq[VariableDefinition]) = if (variables.nonEmpty) {
    variables.foreach(v => GraphQLTranslations.scalaImport(rootPrefix, v.tpe).foreach(x => file.addImport(x._1, x._2)))

    val args = variables.map { v =>
      val typ = GraphQLTranslations.scalaType(v.tpe)
      s"${v.name}: $typ${GraphQLTranslations.defaultVal(typ)}"
    }.mkString(", ")
    val varsDecl = variables.map(v => s""""${v.name}" -> ${v.name}.asJson""").mkString(", ")
    file.add(s"def variables($args) = {", 1)
    file.add(s"Json.obj($varsDecl)")
    file.add("}", -1)
    file.add()
  }

  private[this] def addData(file: ScalaFile, selections: Seq[Selection], nameMap: Map[String, GraphQLQueryParseService.ClassName]) = {
    file.add(s"object Data {", 1)
    file.add(s"implicit val jsonDecoder: Decoder[Data] = deriveDecoder")
    file.add(s"}", -1)

    file.add(s"case class Data(", 1)
    GraphQLQueryHelper.addFields(file, None, selections, nameMap)
    file.add(s")", -1)
    file.add()
  }
  private[this] def addContent(file: ScalaFile, op: OperationDefinition) = {
    file.add("override val content = \"\"\"", 1)
    Source.fromString(op.renderPretty).getLines.foreach(l => file.add("|" + l))
    file.add("\"\"\".stripPrefix.trim", -1)
    file.add()
  }
}
