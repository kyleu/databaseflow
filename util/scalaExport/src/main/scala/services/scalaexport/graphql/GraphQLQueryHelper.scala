package services.scalaexport.graphql

import models.scalaexport.file.ScalaFile
import sangria.ast._
import services.scalaexport.graphql.GraphQLTranslations._

import scala.io.Source

object GraphQLQueryHelper {
  def addQuery(file: ScalaFile, op: OperationDefinition) = {
    file.add("val query = \"\"\"", 1)
    Source.fromString(op.renderPretty).getLines.foreach(l => file.add("|" + l))
    file.add("\"\"\".stripPrefix.trim", -1)
    file.add()
  }

  def addVariables(rootPrefix: String, file: ScalaFile, variables: Seq[VariableDefinition]) = if (variables.nonEmpty) {
    variables.foreach(v => scalaImport(rootPrefix, v.tpe).foreach(x => file.addImport(x._1, x._2)))

    val args = variables.map { v =>
      val typ = scalaType(v.tpe)
      s"${v.name}: $typ${defaultVal(typ)}"
    }.mkString(", ")
    val varsDecl = variables.map(v => s""""${v.name}" -> ${v.name}.asJson""").mkString(", ")
    file.add(s"def variables($args) = {", 1)
    file.add(s"Json.obj($varsDecl)")
    file.add("}", -1)
    file.add()
  }

  def addData(file: ScalaFile, selections: Seq[Selection]) = {
    file.add(s"object Data {", 1)
    file.add(s"implicit val jsonDecoder: Decoder[Data] = deriveDecoder")
    file.add(s"}", -1)
    val members = selections.map {
      case Field(alias, name, _, _, sels, _, _, _) => s"${alias.getOrElse(name)}: Json"
      case s => "/* " + s + "*/"
    }.mkString(", ")

    file.add(s"case class Data($members)")
    file.add()
  }

  def addFields(file: ScalaFile, selections: Seq[Selection]) = selections.foreach { s =>
    val param = s match {
      case Field(alias, name, _, _, sels, _, _, _) => s"${alias.getOrElse(name)}: Json"
      case x => s"/* $x */"
    }
    val comma = if (selections.lastOption.contains(s)) { "" } else { "," }
    file.add(param + comma)
  }
}
