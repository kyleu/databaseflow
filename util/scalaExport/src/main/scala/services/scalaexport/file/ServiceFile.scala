package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.inject.InjectSearchParams
import services.scalaexport.{ExportHelper, ExportTable}

object ServiceFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("services" +: et.pkg, et.className + "Service")
    file.addImport(("models" +: et.pkg).mkString("."), et.className)
    file.addImport(("models" +: "queries" +: et.pkg).mkString("."), et.className + "Queries")
    file.addImport("services.database", "Database")
    file.addImport("services", "ModelServiceHelper")

    file.add(s"object ${et.className}Service extends ModelServiceHelper[${et.className}] {", 1)

    et.pkColumns.foreach(col => col.columnType.requiredImport.foreach(pkg => file.addImport(pkg, col.columnType.asScala)))

    et.pkColumns match {
      case Nil => // noop
      case col :: Nil =>
        val colProp = ExportHelper.toIdentifier(col.name)
        file.add(s"def getById($colProp: ${col.columnType.asScala}) = Database.query(${et.className}Queries.getById($colProp))")
        file.add(s"def getByIdSeq(${colProp}Seq: Seq[${col.columnType.asScala}]) = Database.query(${et.className}Queries.getByIdSeq(${colProp}Seq))")

        col.columnType match {
          case ColumnType.UuidType => file.addMarker("uuid-search", InjectSearchParams(
            pkg = et.pkg, className = et.className, pkColumns = Seq(col.name -> col.columnType.asScalaFull)
          ).toString)
          case ColumnType.IntegerType => file.addMarker("int-search", InjectSearchParams(
            pkg = et.pkg, className = et.className, pkColumns = Seq(col.name -> col.columnType.asScalaFull)
          ).toString)
          case _ => // noop
        }

      case cols => // multiple columns
        val tupleTyp = "(" + cols.map(_.columnType.asScala).mkString(", ") + ")"
        val colArgs = cols.map(c => ExportHelper.toIdentifier(c.name) + ": " + c.columnType.asScala).mkString(", ")
        val queryArgs = cols.map(c => ExportHelper.toIdentifier(c.name)).mkString(", ")
        file.add(s"def getById($colArgs) = Database.query(${et.className}Queries.getById($queryArgs))")
        file.add(s"def getByIdSeq(idSeq: Seq[$tupleTyp]) = Database.query(${et.className}Queries.getByIdSeq(idSeq))")
    }
    file.add()

    file.addMarker("string-search", InjectSearchParams(
      pkg = et.pkg, className = et.className, pkColumns = et.pkColumns.map(c => c.name -> c.columnType.asScalaFull)
    ).toString)

    file.addImport("models.result.filter", "Filter")
    file.addImport("models.result.orderBy", "OrderBy")

    file.add(s"override def countAll(filters: Seq[Filter] = Nil) = Database.query(${et.className}Queries.countAll(filters))")
    file.add("override def getAll(filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int] = None, offset: Option[Int] = None) = {", 1)
    file.add(s"Database.query(${et.className}Queries.getAll(filters, orderBys, limit, offset))")
    file.add("}", -1)
    file.add()
    file.add(s"override def searchCount(q: String, filters: Seq[Filter]) = Database.query(${et.className}Queries.searchCount(q, filters))")
    file.add("override def search(q: String, filters: Seq[Filter], orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]) = {", 1)
    file.add(s"Database.query(${et.className}Queries.search(q, filters, orderBys, limit, offset))")
    file.add("}", -1)
    file.add()
    file.add("def searchExact(q: String, orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]) = {", 1)
    file.add(s"Database.query(${et.className}Queries.searchExact(q, orderBys, limit, offset))")
    file.add("}", -1)
    file.add()

    ForeignKeysHelper.writeService(et, file)

    file.add(s"def insert(model: ${et.className}) = Database.execute(${et.className}Queries.insert(model))")

    et.pkColumns match {
      case Nil => // noop
      case col :: Nil =>
        col.columnType.requiredImport.foreach(x => file.addImport(x, col.columnType.asScala))
        file.add(s"def remove(${col.name}: ${col.columnType.asScala}) = Database.execute(${et.className}Queries.removeById(${col.name}))")
      case _ => // multiple columns
    }

    file.add("}", -1)
    file
  }
}
