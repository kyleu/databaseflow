package services.scalaexport.db.file

import models.scalaexport.db.ExportModel
import models.scalaexport.file.ScalaFile
import services.scalaexport.db.inject.InjectSearchParams

object ServiceFile {
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

    ServiceHelper.writeSearchFields(model, file, queriesFilename, "(implicit trace: TraceData)", searchArgs)
    ServiceHelper.writeForeignKeys(model, file)

    ServiceInserts.insertsFor(model, queriesFilename, file)
    ServiceMutations.mutations(model, file)

    file.add()
    file.add(s"def csvFor(totalCount: Int, rows: Seq[${model.className}])(implicit trace: TraceData) = {", 1)
    file.add(s"""traceB("export.csv")(td => util.CsvUtils.csvFor(Some(key), totalCount, rows, $queriesFilename.fields)(td))""")
    file.add("}", -1)

    file.add("}", -1)
    file
  }
}
