package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.ExportModel
import services.scalaexport.inject.InjectSearchParams

object ServiceFile {
  private[this] val td = "(implicit trace: TraceData)"
  private[this] val inject = "@javax.inject.Inject() (override val tracing: TracingService)"

  def export(model: ExportModel) = {
    val file = ScalaFile(model.servicePackage, model.className + "Service")
    file.addImport("util.FutureUtils", "databaseContext")
    file.addImport(model.modelPackage.mkString("."), model.className)
    file.addImport(model.queriesPackage.mkString("."), model.className + "Queries")
    file.addImport("services.database", "Database")

    file.addImport("models.result.filter", "Filter")
    file.addImport("models.result.orderBy", "OrderBy")

    file.addImport("util.tracing", "TraceData")
    file.addImport("util.tracing", "TracingService")

    if (model.pkg.nonEmpty) {
      file.addImport("services", "ModelServiceHelper")
    }

    file.add("@javax.inject.Singleton")
    file.add(s"class ${model.className}Service $inject extends ModelServiceHelper[${model.className}] {", 1)
    ServiceHelper.addGetters(model, file)
    file.addMarker("string-search", InjectSearchParams(model).toString)

    file.add(s"override def countAll(filters: Seq[Filter] = Nil)$td = Database.query(${model.className}Queries.countAll(filters))")
    file.add(s"override def getAll(filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int] = None, offset: Option[Int] = None)$td = {", 1)
    file.add(s"Database.query(${model.className}Queries.getAll(filters, orderBys, limit, offset))")
    file.add("}", -1)
    file.add()
    file.add(s"override def searchCount(q: String, filters: Seq[Filter])$td = Database.query(${model.className}Queries.searchCount(q, filters))")
    file.add(s"override def search(q: String, filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int])$td = {", 1)
    file.add(s"Database.query(${model.className}Queries.search(q, filters, orderBys, limit, offset))")
    file.add("}", -1)
    file.add()
    file.add(s"def searchExact(q: String, orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int])$td = {", 1)
    file.add(s"Database.query(${model.className}Queries.searchExact(q, orderBys, limit, offset))")
    file.add("}", -1)
    file.add()

    ForeignKeysHelper.writeService(model, file)
    file.add(s"def insert(model: ${model.className})$td = Database.execute(${model.className}Queries.insert(model)).flatMap {", 1)
    if (model.pkFields.isEmpty) {
      file.add(s"case _ => scala.concurrent.Future.successful(None: Option[${model.className}])")
    } else {
      file.add(s"case 1 => getByPrimaryKey(${model.pkFields.map(f => "model." + f.propertyName).mkString(", ")})")
      file.add(s"""case x => throw new IllegalStateException("Unable to find newly-inserted ${model.title}.")""")
    }
    file.add("}", -1)

    file.addImport("models.result.data", "DataField")
    file.add(s"def create(fields: Seq[DataField])$td = Database.execute(${model.className}Queries.create(fields)).map { _ =>", 1)
    file.add(s"None: Option[${model.className}]")
    file.add("}", -1)
    file.add()

    if (model.pkFields.nonEmpty) {
      model.pkFields.foreach(f => f.t.requiredImport.foreach(x => file.addImport(x, f.t.asScala)))
      val sig = model.pkFields.map(f => f.propertyName + ": " + f.t.asScala).mkString(", ")
      val call = model.pkFields.map(_.propertyName).mkString(", ")
      val interp = model.pkFields.map(f => "$" + f.propertyName).mkString(", ")
      file.add(s"def remove($sig)$td = Database.query(${model.className}Queries.getByPrimaryKey($call)).flatMap {", 1)
      file.add(s"case Some(current) => Database.execute(${model.className}Queries.removeByPrimaryKey($call)).map(_ => current)")
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find Note matching [$interp].")""")
      file.add("}", -1)
      file.add()
      file.add(s"def update($sig, fields: Seq[DataField])$td = Database.query(${model.className}Queries.getByPrimaryKey($call)).flatMap {", 1)
      file.add(s"case Some(current) => Database.execute(${model.className}Queries.update($call, fields)).flatMap { _ =>", 1)
      file.add(s"Database.query(${model.className}Queries.getByPrimaryKey($call)).map {", 1)
      file.add("case Some(newModel) => newModel")
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("}", -1)
      file.add("}", -1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp].")""")
      file.add("}", -1)
    }
    file.add("}", -1)
    file
  }
}
