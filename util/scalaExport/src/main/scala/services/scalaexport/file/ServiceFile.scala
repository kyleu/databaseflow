package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportModel
import services.scalaexport.inject.InjectSearchParams

object ServiceFile {
  def export(model: ExportModel) = {
    val file = ScalaFile("services" +: model.pkg, model.className + "Service")
    file.addImport("util.FutureUtils", "databaseContext")
    file.addImport(("models" +: model.pkg).mkString("."), model.className)
    file.addImport(("models" +: "queries" +: model.pkg).mkString("."), model.className + "Queries")
    file.addImport("services.database", "Database")
    if (model.pkg.nonEmpty) {
      file.addImport("services", "ModelServiceHelper")
    }

    file.add(s"object ${model.className}Service extends ModelServiceHelper[${model.className}] {", 1)
    addGetters(model, file)
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

    file.addImport("models.result.data", "DataField")
    file.add(s"def create(fields: Seq[DataField]) = Database.execute(${model.className}Queries.create(fields))")
    file.add()

    if (model.pkColumns.nonEmpty) {
      model.pkColumns.foreach(col => col.columnType.requiredImport.foreach(x => file.addImport(x, col.columnType.asScala)))
      val sig = model.pkColumns.map(c => ExportHelper.toIdentifier(c.name) + ": " + c.columnType.asScala).mkString(", ")
      val call = model.pkColumns.map(c => ExportHelper.toIdentifier(c.name)).mkString(", ")
      val interp = model.pkColumns.map(c => "$" + ExportHelper.toIdentifier(c.name)).mkString(", ")
      file.add(s"def remove($sig) = Database.query(${model.className}Queries.getByPrimaryKey($call)).flatMap {", 1)
      file.add(s"case Some(current) => Database.execute(${model.className}Queries.removeByPrimaryKey($call)).map(_ => current)")
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find Note matching [$interp].")""")
      file.add("}", -1)
      file.add()
      file.add(s"def update($sig, fields: Seq[DataField]) = Database.query(${model.className}Queries.getByPrimaryKey($call)).flatMap {", 1)
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

  private[this] def addGetters(model: ExportModel, file: ScalaFile) = {
    model.pkColumns.foreach(col => col.columnType.requiredImport.foreach(pkg => file.addImport(pkg, col.columnType.asScala)))
    model.pkColumns match {
      case Nil => // noop
      case col :: Nil =>
        val colProp = ExportHelper.toIdentifier(col.name)
        file.add(s"def getByPrimaryKey($colProp: ${col.columnType.asScala}) = Database.query(${model.className}Queries.getByPrimaryKey($colProp))")
        file.add(s"def getByPrimaryKeySeq(${colProp}Seq: Seq[${col.columnType.asScala}]) = Database.query(${model.className}Queries.getByPrimaryKeySeq(${colProp}Seq))")
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
        file.add(s"def getByPrimaryKey($colArgs) = Database.query(${model.className}Queries.getByPrimaryKey($queryArgs))")
        file.add(s"def getByPrimaryKeySeq(pkSeq: Seq[$tupleTyp]) = Database.query(${model.className}Queries.getByPrimaryKeySeq(pkSeq))")
    }
    file.add()
  }
}
