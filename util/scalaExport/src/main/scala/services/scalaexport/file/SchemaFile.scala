package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.ExportTable

object SchemaFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("models" +: et.pkg, et.className + "Schema")

    file.addImport(("services" +: et.pkg).mkString("."), et.className + "Service")

    file.addImport("models.graphql", "CommonSchema")
    file.addImport("models.graphql", "GraphQLContext")
    file.addImport("sangria.macros.derive", "_")
    file.addImport("sangria.schema", "_")
    file.addImport("models.graphql.CommonSchema", "_")
    file.addImport("models.graphql.DateTimeSchema", "_")

    file.add(s"object ${et.className}Schema {", 1)

    et.pkColumns match {
      case Nil => // noop
      case pkCol :: Nil => file.add(s"implicit val ${et.propertyName}Id = HasId[${et.className}, ${pkCol.columnType.asScala}](_.${pkCol.name})")
      case _ => // multiple columns
    }

    file.add(s"implicit val ${et.propertyName}Type = deriveObjectType[GraphQLContext, ${et.className}]()")
    file.add()
    file.add("val queryFields = fields[GraphQLContext, Unit](Field(", 1)

    file.add(s"""name = "${et.propertyName}",""")
    file.add(s"fieldType = ListType(${et.propertyName}Type),")

    file.add(s"resolve = c => ${et.className}Service.getAll()")

    file.add("))", -1)
    file.add("}", -1)
    file
  }
}
