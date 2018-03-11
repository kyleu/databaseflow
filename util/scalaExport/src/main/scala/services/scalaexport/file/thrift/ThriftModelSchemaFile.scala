package services.scalaexport.file.thrift

import models.scalaexport.ScalaFile
import models.scalaexport.thrift.ThriftStruct

object ThriftModelSchemaFile {
  def export(
    srcPkg: Seq[String],
    tgtPkg: Seq[String],
    model: ThriftStruct,
    typedefs: Map[String, String],
    enums: Map[String, String],
    pkgMap: Map[String, Seq[String]]
  ) = {
    val file = ScalaFile(tgtPkg :+ "graphql", model.name + "Schema")

    file.addImport("models.graphql", "GraphQLContext")
    file.addImport("sangria.macros.derive", "AddFields")
    file.addImport("sangria.macros.derive", "deriveObjectType")
    file.addImport("sangria.schema", "_")

    ThriftSchemaHelper.addImports(tgtPkg, model.fields.map(_.t), typedefs, pkgMap, file)

    file.add(s"""object ${model.name}Schema {""", 1)
    file.add(s"implicit lazy val ${model.identifier}Type: ObjectType[GraphQLContext, ${tgtPkg.mkString(".")}.${model.name}] = deriveObjectType(", 1)

    val replacedFields = ThriftSchemaHelper.getReplaceFields(tgtPkg, model.fields.map(x => (x.name, x.required, x.t)), typedefs, pkgMap)
    if (replacedFields.nonEmpty) {
      file.addImport("sangria.macros.derive", "ReplaceField")
      replacedFields.foreach(f => file.add(f.fullFieldDecl + ","))
    }
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
