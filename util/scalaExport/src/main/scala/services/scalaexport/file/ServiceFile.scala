package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportConfiguration
import services.scalaexport.inject.InjectSearchParams

object ServiceFile {
  def export(model: ExportConfiguration.Model) = {
    val file = ScalaFile("services" +: model.pkg, model.className + "Service")
    file.addImport(("models" +: model.pkg).mkString("."), model.className)
    file.addImport(("models" +: "queries" +: model.pkg).mkString("."), model.className + "Queries")
    file.addImport("services.database", "Database")
    file.addImport("services", "ModelServiceHelper")

    file.add(s"object ${model.className}Service extends ModelServiceHelper[${model.className}] {", 1)

    model.pkColumns.foreach(col => col.columnType.requiredImport.foreach(pkg => file.addImport(pkg, col.columnType.asScala)))

    model.pkColumns match {
      case Nil => // noop
      case col :: Nil =>
        val colProp = ExportHelper.toIdentifier(col.name)
        file.add(s"def getById($colProp: ${col.columnType.asScala}) = Database.query(${model.className}Queries.getById($colProp))")
        file.add(s"def getByIdSeq(${colProp}Seq: Seq[${col.columnType.asScala}]) = Database.query(${model.className}Queries.getByIdSeq(${colProp}Seq))")

        col.columnType match {
          case ColumnType.UuidType => file.addMarker("uuid-search", InjectSearchParams(
            pkg = model.pkg, className = model.className, pkColumns = Seq(col.name -> col.columnType.asScalaFull)
          ).toString)
          case ColumnType.IntegerType => file.addMarker("int-search", InjectSearchParams(
            pkg = model.pkg, className = model.className, pkColumns = Seq(col.name -> col.columnType.asScalaFull)
          ).toString)
          case _ => // noop
        }

      case cols => // multiple columns
        val tupleTyp = "(" + cols.map(_.columnType.asScala).mkString(", ") + ")"
        val colArgs = cols.map(c => ExportHelper.toIdentifier(c.name) + ": " + c.columnType.asScala).mkString(", ")
        val queryArgs = cols.map(c => ExportHelper.toIdentifier(c.name)).mkString(", ")
        file.add(s"def getById($colArgs) = Database.query(${model.className}Queries.getById($queryArgs))")
        file.add(s"def getByIdSeq(idSeq: Seq[$tupleTyp]) = Database.query(${model.className}Queries.getByIdSeq(idSeq))")
    }
    file.add()

    file.addMarker("string-search", InjectSearchParams(
      pkg = model.pkg, className = model.className, pkColumns = model.pkColumns.map(c => c.name -> c.columnType.asScalaFull)
    ).toString)

    file.addImport("models.result.filter", "Filter")
    file.addImport("models.result.orderBy", "OrderBy")

    file.add(s"override def countAll(filters: Seq[Filter] = Nil) = Database.query(${model.className}Queries.countAll(filters))")
    file.add("override def getAll(filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int] = None, offset: Option[Int] = None) = {", 1)
    file.add(s"Database.query(${model.className}Queries.getAll(filters, orderBys, limit, offset))")
    file.add("}", -1)
    file.add()
    file.add(s"override def searchCount(q: String, filters: Seq[Filter]) = Database.query(${model.className}Queries.searchCount(q, filters))")
    file.add("override def search(q: String, filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]) = {", 1)
    file.add(s"Database.query(${model.className}Queries.search(q, filters, orderBys, limit, offset))")
    file.add("}", -1)
    file.add()
    file.add("def searchExact(q: String, orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]) = {", 1)
    file.add(s"Database.query(${model.className}Queries.searchExact(q, orderBys, limit, offset))")
    file.add("}", -1)
    file.add()

    ForeignKeysHelper.writeService(model, file)

    file.add(s"def insert(model: ${model.className}) = Database.execute(${model.className}Queries.insert(model))")

    if (model.pkColumns.nonEmpty) {
      file.addImport("models.result.data", "DataField")
      model.pkColumns.foreach(col => col.columnType.requiredImport.foreach(x => file.addImport(x, col.columnType.asScala)))
      val sig = model.pkColumns.map(c => ExportHelper.toIdentifier(c.name) + ": " + c.columnType.asScala).mkString(", ")
      val call = model.pkColumns.map(c => ExportHelper.toIdentifier(c.name)).mkString(", ")
      file.add(s"def remove($sig) = Database.execute(${model.className}Queries.removeById($call))")
      file.add()
      file.add(s"def update($sig, fields: Seq[DataField]) = Database.execute(${model.className}Queries.update($call, fields))")
    }

    file.add("}", -1)
    file
  }
}
