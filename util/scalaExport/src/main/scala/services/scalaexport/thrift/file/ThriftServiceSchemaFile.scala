package services.scalaexport.thrift.file

import models.scalaexport.file.ScalaFile
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
    file.addImport("graphql", "GraphQLContext")
    file.addImport("graphql", "GraphQLSchemaHelper")
    file.addImport("sangria.schema", "_")
    file.addImport("util.FutureUtils", "graphQlContext")
    file.addImport("sangria.marshalling.circe", "_")

    file.add(s"""object ${service.name}Schema extends GraphQLSchemaHelper("${service.name}") {""", 1)

    file.add(s"private[this] val ${service.identifier}Fields = fields[GraphQLContext, ${service.name}](", 1)
    service.methods.foreach(m => addMethodField(tgtPkg, m, metadata, file))
    file.add("Field(", 1)
    file.add("""name = "healthcheck",""")
    file.add("fieldType = StringType,")
    file.add(s"""resolve = c => traceF(c.ctx, "healthcheck")(td => c.value.healthcheck(td))""")
    file.add(")", -1)
    file.add(")", -1)
    file.add()
    val objType = s"""ObjectType(name = "${service.name}", fields = ${service.identifier}Fields)"""
    file.add(s"lazy val ${service.identifier}Type = $objType")
    file.add("}", -1)

    file
  }

  private[this] def addMethodField(pkg: Seq[String], m: ThriftServiceMethod, metadata: ThriftMetadata, file: ScalaFile) = {
    ThriftSchemaHelper.addImports(pkg = pkg, types = Seq(m.returnValue), metadata = metadata, file = file)
    ThriftSchemaInputHelper.addInputImports(pkg = pkg, types = m.arguments.map(_.t), metadata = metadata, file = file)

    val retType = ThriftFileHelper.columnTypeFor(m.returnValue, metadata)._1
    val retGraphQlType = ThriftSchemaHelper.graphQlTypeFor(retType)

    val retMaps = ThriftSchemaHelper.mapsFor(retType) match {
      case _ if retType == "Unit" => ".map(_ => true)"
      case x if x.nonEmpty => s".map(_$x)"
      case _ => ""
    }

    if (m.arguments.isEmpty) {
      file.add("Field(", 1)
      file.add(s"""name = "${m.name}",""")
      file.add(s"fieldType = $retGraphQlType,")
      file.add(s"""resolve = c => traceF(c.ctx, "${m.name}")(td => c.value.${m.name}()(td)$retMaps)""")
      file.add("),", -1)
    } else {
      file.add("{", 1)
      m.arguments.foreach { arg =>
        val argType = ThriftFileHelper.columnTypeFor(arg.t, metadata)._1
        val argInputType = ThriftSchemaInputHelper.graphQlInputTypeFor(Some(pkg -> file), argType, metadata.enums, arg.required || arg.value.isDefined)
        file.add(s"""val ${arg.name}Arg = Argument(name = "${arg.name}", argumentType = $argInputType)""")
      }
      file.add("Field(", 1)
      file.add(s"""name = "${m.name}",""")
      file.add(s"fieldType = $retGraphQlType,")
      file.add(s"arguments = ${m.arguments.map(arg => arg.name + "Arg :: ").mkString}Nil,")

      file.add(s"""resolve = c => traceF(c.ctx, "${m.name}") { td =>""", 1)
      val argsRefs = m.arguments.map { arg =>
        val argType = ThriftFileHelper.columnTypeFor(arg.t, metadata)._1
        val mapped = ThriftSchemaInputHelper.mapsFor(argType, arg.required || arg.value.isDefined)
        s"${arg.name} = c.arg(${arg.name}Arg)$mapped"
      }
      file.add(s"c.value.${m.name}(${argsRefs.mkString(", ")})(td)$retMaps")
      file.add("}", -1)
      file.add(")", -1)
      file.add("},", -1)
    }
  }
}
