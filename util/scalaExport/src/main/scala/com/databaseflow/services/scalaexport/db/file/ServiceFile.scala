package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.ScalaFile
import com.databaseflow.services.scalaexport.db.inject.InjectSearchParams

object ServiceFile {
  private[this] val inject = "@javax.inject.Inject() (override val tracing: TracingService)"
  private[this] val searchArgs = "filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None"

  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(pkg = model.servicePackage, key = model.className + "Service", core = true)
    val queriesFilename = model.className + "Queries"

    file.addImport(model.modelPackage.mkString("."), model.className)
    file.addImport(model.queriesPackage.mkString("."), model.className + "Queries")
    file.addImport("scala.concurrent", "Future")
    file.addImport(config.providedPrefix + "services.database", "ApplicationDatabase")
    file.addImport(config.providedPrefix + "util.FutureUtils", "serviceContext")
    file.addImport(config.providedPrefix + "models.result.data", "DataField")
    file.addImport(config.corePrefix + "models.auth", "Credentials")
    file.addImport(config.providedPrefix + "models.result.filter", "Filter")
    file.addImport(config.providedPrefix + "models.result.orderBy", "OrderBy")

    file.addImport(config.providedPrefix + "util.tracing", "TraceData")
    file.addImport(config.providedPrefix + "util.tracing", "TracingService")

    if (model.pkg.nonEmpty) {
      file.addImport(config.providedPrefix + "services", "ModelServiceHelper")
    }

    file.add("@javax.inject.Singleton")
    file.add(s"""class ${model.className}Service $inject extends ModelServiceHelper[${model.className}]("${model.propertyName}") {""", 1)
    ServiceHelper.addGetters(model, file)
    if (model.propertyName != "audit") {
      file.addMarker("string-search", InjectSearchParams(model).toString)
    }

    ServiceHelper.writeSearchFields(model, file, queriesFilename, "(implicit trace: TraceData)", searchArgs)
    ServiceHelper.writeForeignKeys(model, file)

    ServiceInserts.insertsFor(config.providedPrefix, model, queriesFilename, file)
    ServiceMutations.mutations(config.providedPrefix, model, file)

    file.add()
    file.add(s"def csvFor(totalCount: Int, rows: Seq[${model.className}])(implicit trace: TraceData) = {", 1)
    file.add(s"""traceB("export.csv")(td => ${config.providedPrefix}util.CsvUtils.csvFor(Some(key), totalCount, rows, $queriesFilename.fields)(td))""")
    file.add("}", -1)

    file.add("}", -1)
    file
  }
}
