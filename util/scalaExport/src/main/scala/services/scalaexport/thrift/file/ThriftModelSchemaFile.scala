package services.scalaexport.thrift.file

import models.scalaexport.file.ScalaFile
import models.scalaexport.thrift.{ThriftMetadata, ThriftStruct}

object ThriftModelSchemaFile {
  def export(
    srcPkg: Seq[String],
    tgtPkg: Seq[String],
    model: ThriftStruct,
    metadata: ThriftMetadata
  ) = {
    val file = ScalaFile(tgtPkg :+ "graphql", model.name + "Schema")

    file.addImport("models.graphql", "GraphQLContext")
    file.addImport("sangria.macros.derive", "AddFields")
    file.addImport("sangria.macros.derive", "ObjectTypeName")
    file.addImport("sangria.macros.derive", "InputObjectTypeName")
    file.addImport("sangria.macros.derive", "deriveObjectType")
    file.addImport("sangria.macros.derive", "deriveInputObjectType")
    file.addImport("sangria.schema", "_")
    file.addImport("sangria.marshalling.circe", "_")
    file.addImport(tgtPkg.mkString("."), model.name)
    ThriftSchemaHelper.addImports(pkg = tgtPkg, types = model.fields.map(_.t), metadata = metadata, file = file)

    file.add(s"""object ${model.name}Schema {""", 1)

    val replacedInputFields = ThriftSchemaInputHelper.getReplaceInputFields(
      file, tgtPkg, model.fields.map(x => (x.name, x.required || x.value.isDefined, x.t)), metadata
    )
    file.add(s"implicit lazy val ${model.identifier}InputType: InputType[${model.name}] = deriveInputObjectType[${model.name}](", 1)
    ThriftSchemaInputHelper.addInputImports(pkg = tgtPkg, types = model.fields.map(_.t), metadata = metadata, file = file)
    if (replacedInputFields.nonEmpty) {
      file.addImport("sangria.macros.derive", "ReplaceInputField")
      replacedInputFields.foreach(f => file.add(f.fullFieldDecl + ","))
    }
    file.add(s"""InputObjectTypeName("Thrift${model.name}Input")""")
    file.add(")", -1)
    file.add()

    file.add(s"implicit lazy val ${model.identifier}Type: ObjectType[GraphQLContext, ${model.name}] = deriveObjectType(", 1)
    file.add(s"""ObjectTypeName("Thrift${model.name}"),""")
    val replacedFields = ThriftSchemaHelper.getReplaceFields(tgtPkg, model.fields.map(x => (x.name, x.required || x.value.isDefined, x.t)), metadata)
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
