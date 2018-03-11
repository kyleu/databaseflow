package services.scalaexport.file.thrift

import models.scalaexport.ScalaFile
import models.scalaexport.thrift.ThriftService

object ThriftServiceSchemaFile {
  def export(
    srcPkg: Seq[String],
    tgtPkg: Seq[String],
    service: ThriftService,
    typedefs: Map[String, String],
    enums: Map[String, String],
    pkgMap: Map[String, Seq[String]]
  ) = {
    val file = ScalaFile(tgtPkg :+ "graphql", service.name + "Schema")

    file.addImport("models.graphql", "GraphQLContext")
    file.addImport("models.graphql", "SchemaHelper")
    file.addImport("sangria.macros.derive", "AddFields")
    file.addImport("sangria.macros.derive", "deriveObjectType")
    file.addImport("sangria.schema", "_")

    file.add(s"""object ${service.name}Schema extends SchemaHelper("${service.name}") {""", 1)
    file.add(s"implicit lazy val ${service.identifier}Type: ObjectType[GraphQLContext, ${tgtPkg.mkString(".")}.${service.name}] = deriveObjectType(", 1)

    file.add(s"AddFields(Field(", 1)
    file.add("""name = "toString",""")
    file.add("fieldType = StringType,")
    file.add("resolve = c => c.value.toString")
    file.add("))", -1)

    file.add(")", -1)
    file.add("}", -1)

    file
  }
}
