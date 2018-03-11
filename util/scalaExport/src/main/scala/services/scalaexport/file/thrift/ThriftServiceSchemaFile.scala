package services.scalaexport.file.thrift

import models.scalaexport.ScalaFile
import models.scalaexport.thrift.{ThriftMetadata, ThriftService, ThriftServiceMethod}

object ThriftServiceSchemaFile {
  def export(
    srcPkg: Seq[String],
    tgtPkg: Seq[String],
    service: ThriftService,
    metadata: ThriftMetadata
  ) = {
    val file = ScalaFile(tgtPkg :+ "graphql", service.name + "Schema")

    file.addImport(tgtPkg.mkString("."), service.name)
    file.addImport("models.graphql", "GraphQLContext")
    file.addImport("models.graphql", "SchemaHelper")
    file.addImport("sangria.schema", "_")
    file.addImport("util.FutureUtils", "graphQlContext")

    file.add(s"""object ${service.name}Schema extends SchemaHelper("${service.name}") {""", 1)

    file.add(s"private[this] val ${service.identifier}Fields = fields[GraphQLContext, ${service.name}](", 1)
    service.methods.foreach(m => addMethodField(tgtPkg, m, metadata, file))
    file.add("Field(", 1)
    file.add("""name = "healthcheck",""")
    file.add("fieldType = StringType,")
    file.add(s"""resolve = c => traceF(c.ctx, "healthcheck")(td => c.value.healthcheck(td))""")
    file.add(")", -1)
    file.add(")", -1)
    file.add()
    val objType = s"""ObjectType(name = "${service.name}", description = "Thrift service", fields = ${service.identifier}Fields)"""
    file.add(s"lazy val ${service.identifier}Type = $objType")
    file.add("}", -1)

    file
  }

  private[this] def addMethodField(pkg: Seq[String], m: ThriftServiceMethod, metadata: ThriftMetadata, file: ScalaFile) = {
    ThriftSchemaHelper.addImports(pkg = pkg, types = m.returnValue +: m.arguments.map(_.t), metadata = metadata, file = file)

    val retType = ThriftFileHelper.columnTypeFor(m.returnValue, metadata)._1
    val retGraphQlType = ThriftSchemaHelper.graphQlTypeFor(retType)

    file.add("Field(", 1)
    file.add(s"""name = "${m.name}",""")
    file.add(s"fieldType = $retGraphQlType,")

    val retMaps = ThriftSchemaHelper.mapsFor(retType) match {
      case _ if retType == "Unit" => ".map(_ => true)"
      case x if x.nonEmpty => s".map(_$x)"
      case _ => ""
    }

    if (m.arguments.isEmpty) {
      file.add(s"""resolve = c => traceF(c.ctx, "${m.name}")(td => c.value.${m.name}()(td)$retMaps)""")
    } else {
      val args = Seq.empty[String]
      file.add(s"arguments = ${args.map(a => a + " :: ").mkString}Nil,")

      file.add(s"""resolve = c => traceF(c.ctx, "${m.name}") { td =>""", 1)
      val argsRefs = args.map(_.toString)
      file.add(s"c.value.${m.name}(${argsRefs.mkString(", ")})(td)$retMaps")
      file.add("}", -1)
    }
    file.add("),", -1)
  }
}
