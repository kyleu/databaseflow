package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportModel
import services.scalaexport.inject.InjectSearchParams

object ServiceFile {
  private[this] val trace = "(implicit trace: TraceData)"
  private[this] val inject = "@javax.inject.Inject() (override val tracing: TracingService)"
  private[this] val searchArgs = "filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None"

  def export(model: ExportModel) = {
    val file = ScalaFile(model.servicePackage, model.className + "Service")
    val queriesFilename = model.className + "Queries"

    file.addImport(model.modelPackage.mkString("."), model.className)
    file.addImport(model.queriesPackage.mkString("."), model.className + "Queries")
    file.addImport("scala.concurrent", "Future")
    file.addImport("services.database", "ApplicationDatabase")
    file.addImport("util.FutureUtils", "serviceContext")
    file.addImport("models.result.data", "DataField")
    file.addImport("models.auth", "Credentials")
    file.addImport("models.result.filter", "Filter")
    file.addImport("models.result.orderBy", "OrderBy")

    file.addImport("util.tracing", "TraceData")
    file.addImport("util.tracing", "TracingService")

    if (model.pkg.nonEmpty) {
      file.addImport("services", "ModelServiceHelper")
    }

    file.add("@javax.inject.Singleton")
    file.add(s"""class ${model.className}Service $inject extends ModelServiceHelper[${model.className}]("${model.propertyName}") {""", 1)
    ServiceHelper.addGetters(model, file)
    if (model.propertyName != "audit") {
      file.addMarker("string-search", InjectSearchParams(model).toString)
    }

    ServiceHelper.writeSearchFields(model, file, queriesFilename, trace, searchArgs)
    ServiceHelper.writeForeignKeys(model, file)

    insertsFor(model, queriesFilename, file)
    ServiceMutations.mutations(model, file)

    file.add()
    file.add(s"def csvFor(operation: String, totalCount: Int, rows: Seq[${model.className}])(implicit trace: TraceData) = {", 1)
    file.add(s"""traceB("export.csv")(td => util.CsvUtils.csvFor(Some(key), totalCount, rows, $queriesFilename.fields)(td))""")
    file.add("}", -1)

    file.add("}", -1)
    file
  }

  private[this] def insertsFor(model: ExportModel, queriesFilename: String, file: ScalaFile) = {
    file.add("// Mutations")
    file.add(s"""def insert(creds: Credentials, model: ${model.className})$trace = traceF("insert") { td =>""", 1)
    file.add(s"""ApplicationDatabase.executeF($queriesFilename.insert(model))(td).flatMap {""", 1)
    if (model.pkFields.isEmpty) {
      file.add(s"case _ => scala.concurrent.Future.successful(None: Option[${model.className}])")
    } else {
      if (model.audited) {
        file.add(s"case 1 => getByPrimaryKey(creds, ${model.pkFields.map(f => "model." + f.propertyName).mkString(", ")})(td).map {", 1)
        val audit = model.pkFields.map(f => "n." + f.propertyName + ".toString").mkString(", ")
        file.add("case Some(n) =>")
        file.add(s"""  services.audit.AuditHelper.onInsert("${model.className}", Seq($audit), n.toDataFields, creds)""")
        file.add("  model")
        file.add(s"""case None => throw new IllegalStateException("Unable to find ${model.title}.")""")
        file.add("}", -1)
      } else {
        file.add(s"case 1 => getByPrimaryKey(creds, ${model.pkFields.map(f => "model." + f.propertyName).mkString(", ")})(td)")
      }
      file.add(s"""case _ => throw new IllegalStateException("Unable to find newly-inserted ${model.title}.")""")
    }
    file.add("}", -1)
    file.add("}", -1)

    file.add(s"def insertBatch(creds: Credentials, models: Seq[${model.className}])$trace = {", 1)
    file.add(s"""traceF("insertBatch")(td => ApplicationDatabase.executeF($queriesFilename.insertBatch(models))(td))""")
    file.add("}", -1)

    file.add(s"""def create(creds: Credentials, fields: Seq[DataField])$trace = traceF("create") { td =>""", 1)
    file.add(s"""ApplicationDatabase.executeF($queriesFilename.create(fields))(td).flatMap { _ =>""", 1)
    model.pkFields match {
      case Nil => file.add(s"None: Option[${model.className}]")
      case pk =>
        val lookup = pk.map(k => k.fromString(s"""fieldVal(fields, "${k.propertyName}")""")).mkString(", ")
        if (model.audited) {
          val audit = pk.map(k => s"""fieldVal(fields, "${k.propertyName}")""").mkString(", ")
          file.add(s"""services.audit.AuditHelper.onInsert("${model.className}", Seq($audit), fields, creds)""")
        }
        file.add(s"getByPrimaryKey(creds, $lookup)")
    }
    file.add("}", -1)
    file.add("}", -1)
  }
}
