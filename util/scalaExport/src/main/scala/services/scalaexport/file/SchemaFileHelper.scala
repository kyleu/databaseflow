package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.{ExportHelper, ExportTable}

object SchemaFileHelper {
  def addImports(file: ScalaFile) = {
    file.addImport("models.graphql", "CommonSchema")
    file.addImport("models.graphql", "GraphQLContext")
    file.addImport("sangria.macros.derive", "_")
    file.addImport("sangria.schema", "_")
    file.addImport("models.graphql.CommonSchema", "_")
    file.addImport("models.graphql.DateTimeSchema", "_")
  }

  def addPrimaryKey(et: ExportTable, file: ScalaFile) = et.pkColumns match {
    case Nil => // noop
    case pkCol :: Nil =>
      file.addImport("sangria.execution.deferred", "HasId")
      pkCol.columnType.requiredImport.foreach(pkg => file.addImport(pkg, pkCol.columnType.asScala))
      pkCol.columnType.requiredImport.foreach(x => file.addImport(x, pkCol.columnType.asScala))
      file.add(s"implicit val ${et.propertyName}Id = HasId[${et.className}, ${pkCol.columnType.asScala}](_.${ExportHelper.toIdentifier(pkCol.name)})")
      file.add()

      file.addImport("sangria.execution.deferred", "Fetcher")
      val fetcherName = s"${et.propertyName}By${ExportHelper.toClassName(pkCol.name)}Fetcher"
      val pn = ExportHelper.toIdentifier(pkCol.name) + "Seq"
      file.addMarker("fetcher", (file.pkg :+ (et.className + "Schema")).mkString(".") + "." + fetcherName)
      file.add(s"val $fetcherName = Fetcher((_: GraphQLContext, $pn: Seq[${pkCol.columnType.asScala}]) => ${et.className}Service.getByIdSeq($pn))")
      file.add()
    case pkCols =>
      pkCols.foreach(pkCol => pkCol.columnType.requiredImport.foreach(pkg => file.addImport(pkg, pkCol.columnType.asScala)))
      file.addImport("sangria.execution.deferred", "HasId")
      val method = "x => (" + pkCols.map(c => "x." + ExportHelper.toIdentifier(c.name)).mkString(", ") + ")"
      file.add(s"implicit val ${et.propertyName}Id = HasId[${et.className}, ${et.pkType.getOrElse("String")}]($method)")
      file.add()

      file.addImport("sangria.execution.deferred", "Fetcher")
      val fetcherName = s"${et.propertyName}ByIdFetcher"
      file.addMarker("fetcher", (file.pkg :+ (et.className + "Schema")).mkString(".") + "." + fetcherName)
      file.add(s"val $fetcherName = Fetcher((_: GraphQLContext, idSeq: Seq[${et.pkType.getOrElse("String")}]) => ${et.className}Service.getByIdSeq(idSeq))")
      file.add()

  }

  def addReferences(et: ExportTable, file: ScalaFile) = if (et.references.nonEmpty) {
    file.add("AddFields(", 1)
    et.references.foreach { ref =>
      if (ref.pkg != et.pkg) {
        file.addImport(("models" +: ref.pkg).mkString("."), ref.cls + "Schema")
      }
      file.add("Field(", 1)
      file.add(s"""name = "${ref.name}",""")
      file.add(s"""fieldType = ListType(${ref.cls}Schema.${ExportHelper.toIdentifier(ref.cls)}Type),""")
      file.add(s"""arguments = CommonSchema.limitArg :: CommonSchema.offsetArg :: Nil,""")

      val fetcherRef = s"${ref.cls}Schema.${ExportHelper.toIdentifier(ref.cls)}By${ExportHelper.toClassName(ref.prop)}Fetcher"
      if (ref.notNull) {
        file.add(s"resolve = ctx => $fetcherRef.deferSeq(Seq(ctx.value.${ref.tgt}))")
      } else {
        file.add(s"resolve = ctx => $fetcherRef.deferSeq(ctx.value.${ref.tgt}.toSeq)")
      }
      val comma = if (et.references.lastOption.contains(ref)) { "" } else { "," }
      file.add(")" + comma, -1)
    }
    file.add(")", -1)
  }
}
