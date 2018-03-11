package services.scalaexport.file.thrift

import models.scalaexport.ScalaFile
import models.scalaexport.thrift.{ThriftIntegerEnum, ThriftStringEnum, ThriftStruct}
import services.scalaexport.ExportHelper

object ThriftEnumSchemaFile {
  def exportString(pkg: Seq[String], e: ThriftStringEnum) = {
    export(pkg, e.name, e.values, "String")
  }

  def exportInt(pkg: Seq[String], e: ThriftIntegerEnum) = {
    export(pkg, e.name, e.fields.map(f => f._1), "Int")
  }

  def export(pkg: Seq[String], name: String, vals: Seq[String], t: String) = {
    val file = ScalaFile(pkg :+ "graphql", name + "Schema")

    file.addImport(pkg.mkString("."), name)
    file.addImport("models.graphql.CommonSchema", s"derive${t}EnumeratumType")

    file.add(s"""object ${name}Schema {""", 1)
    file.add(s"""implicit val ${ExportHelper.toIdentifier(name)}Type = derive${t}EnumeratumType("$name", "", $name.values.map(x => x -> x.toString))""")
    file.add("}", -1)

    file
  }
}
