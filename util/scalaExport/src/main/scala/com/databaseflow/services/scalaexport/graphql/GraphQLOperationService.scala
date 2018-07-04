package com.databaseflow.services.scalaexport.graphql

import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import sangria.ast._
import sangria.schema.{Schema, Type => Typ}

import scala.io.Source

object GraphQLOperationService {
  import GraphQLQueryParseService._

  val shouldAddContent = true

  def opFile(cfg: GraphQLExportConfig, n: String, d: OperationDefinition, nameMap: Map[String, ClassName], schema: Schema[_, _]) = {
    val cn = nameMap(n)
    val file = ScalaFile(cn.pkg, cn.cn, Some(""))
    val rp = if (cfg.providedPrefix.isEmpty) { "models." } else { cfg.providedPrefix }
    file.addImport(cfg.providedPrefix + "util.JsonSerializers", "_")
    file.addImport(rp + "graphql", "GraphQLQuery")

    meaningfulComments(d.comments).foreach(c => file.add("// " + c))

    file.add(s"""object ${cn.cn} {""", 1)
    addVariables(cfg.providedPrefix, file, d.variables, nameMap)
    val typ: Typ = if (d.operationType == OperationType.Query) {
      schema.query
    } else if (d.operationType == OperationType.Mutation) {
      schema.mutation.get
    } else {
      throw new IllegalStateException(d.operationType.toString)
    }
    GraphQLObjectHelper.objectFor(cfg, file, cn.copy(cn = "Data"), typ, d.selections, nameMap, schema)

    file.add()
    if (shouldAddContent) {
      file.add(s"""val query = new GraphQLQuery[Data]("${cn.cn}") { """, 1)
      addContent(file, d)
      file.add("}", -1)
    } else {
      file.add(s"""val query = new GraphQLQuery[Data]("${cn.cn}")""")
    }
    file.add("}", -1)
    Some(file)
  }

  private[this] def addVariables(
    providedPrefix: String, file: ScalaFile, variables: Seq[VariableDefinition], nameMap: Map[String, ClassName]
  ) = if (variables.nonEmpty) {
    variables.foreach(v => GraphQLInputTranslations.scalaImport(providedPrefix, v.tpe, nameMap).foreach(x => file.addImport(x._1, x._2)))

    val args = variables.map { v =>
      val typ = GraphQLInputTranslations.scalaType(v.tpe)
      val typName = v.tpe.namedType.name
      nameMap.get(typName).foreach(x => file.addImport(x.pkg.mkString("."), x.cn))
      s"${v.name}: $typ${GraphQLInputTranslations.defaultVal(typ)}"
    }.mkString(", ")
    val varsDecl = variables.map(v => s""""${v.name}" -> ${v.name}.asJson""").mkString(", ")
    file.add(s"def variables($args) = {", 1)
    file.addImport("io.circe", "Json")
    file.add(s"Json.obj($varsDecl)")
    file.add("}", -1)
  }

  private[this] def addContent(file: ScalaFile, op: OperationDefinition) = {
    file.add("override val content = \"\"\"", 1)
    Source.fromString(op.renderPretty).getLines.foreach(l => file.add("|" + l))
    file.add("\"\"\".stripMargin.trim", -1)
  }
}
