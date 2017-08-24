package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportModel
import services.scalaexport.inject.InjectSearchParams

object ServiceFile {
  private[this] val trace = "(implicit trace: TraceData)"
  private[this] val inject = "@javax.inject.Inject() (override val tracing: TracingService)"
  private[this] val searchArgs = "filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int] = None"

  def export(model: ExportModel) = {
    val file = ScalaFile(model.servicePackage, model.className + "Service")
    file.addImport("util.FutureUtils", "databaseContext")
    file.addImport(model.modelPackage.mkString("."), model.className)
    file.addImport(model.queriesPackage.mkString("."), model.className + "Queries")
    file.addImport("services.database", "MasterDatabase")
    file.addImport("models.result.data", "DataField")

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
    file.addMarker("string-search", InjectSearchParams(model).toString)

    file.add(s"override def countAll(filters: Seq[Filter] = Nil)$trace = {", 1)
    file.add(s"""traceF("get.all.count")(td => MasterDatabase.query(${model.className}Queries.countAll(filters))(td))""")
    file.add("}", -1)
    file.add(s"override def getAll($searchArgs)$trace = {", 1)
    file.add(s"""traceF("get.all")(td => MasterDatabase.query(${model.className}Queries.getAll(filters, orderBys, limit, offset))(td))""")
    file.add("}", -1)
    file.add()
    file.add("// Search")
    file.add(s"override def searchCount(q: String, filters: Seq[Filter])$trace = {", 1)
    file.add(s"""traceF("search.count")(td => MasterDatabase.query(${model.className}Queries.searchCount(q, filters))(td))""")
    file.add("}", -1)
    file.add(s"override def search(q: String, $searchArgs)$trace = {", 1)
    file.add(s"""traceF("search")(td => MasterDatabase.query(${model.className}Queries.search(q, filters, orderBys, limit, offset))(td))""")
    file.add("}", -1)
    file.add()
    file.add(s"def searchExact(q: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None)$trace = {", 1)
    file.add(s"""traceF("search.exact")(td => MasterDatabase.query(${model.className}Queries.searchExact(q, orderBys, limit, offset))(td))""")
    file.add("}", -1)
    file.add()

    ServiceHelper.writeForeignKeys(model, file)

    file.add("// Mutations")
    file.add(s"def insert(model: ${model.className})$trace = {", 1)
    file.add(s"""traceF("insert")(td => MasterDatabase.execute(${model.className}Queries.insert(model))(td).flatMap {""", 1)
    if (model.pkFields.isEmpty) {
      file.add(s"case _ => scala.concurrent.Future.successful(None: Option[${model.className}])")
    } else {
      file.add(s"case 1 => getByPrimaryKey(${model.pkFields.map(f => "model." + f.propertyName).mkString(", ")})(td)")
      file.add(s"""case x => throw new IllegalStateException("Unable to find newly-inserted ${model.title}.")""")
    }
    file.add("})", -1)
    file.add("}", -1)

    file.add(s"def create(fields: Seq[DataField])$trace = {", 1)
    file.add(s"""traceF("create")(td => MasterDatabase.execute(${model.className}Queries.create(fields))(td)).map { _ =>""", 1)
    file.add(s"None: Option[${model.className}]")
    file.add("}", -1)
    file.add("}", -1)
    file.add()

    ServiceHelper.mutations(model, file)

    file.add("}", -1)
    file
  }
}
