package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.ColumnType
import services.scalaexport.config.ExportModel
import services.scalaexport.inject.InjectSearchParams

object ServiceFile {
  def export(model: ExportModel) = {
    val file = ScalaFile(model.servicePackage, model.className + "Service")
    file.addImport("util.FutureUtils", "databaseContext")
    file.addImport(model.modelPackage.mkString("."), model.className)
    file.addImport(model.queriesPackage.mkString("."), model.className + "Queries")
    file.addImport("services.database", "Database")
    if (model.pkg.nonEmpty) {
      file.addImport("services", "ModelServiceHelper")
    }

    file.add(s"object ${model.className}Service extends ModelServiceHelper[${model.className}] {", 1)
    addGetters(model, file)
    file.addMarker("string-search", InjectSearchParams(model).toString)

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
    file.add(s"def insert(model: ${model.className}) = Database.execute(${model.className}Queries.insert(model)).flatMap {", 1)
    if (model.pkFields.isEmpty) {
      file.add(s"case _ => scala.concurrent.Future.successful(None: Option[${model.className}])")
    } else {
      file.add(s"case 1 => getByPrimaryKey(${model.pkFields.map(f => "model." + f.propertyName).mkString(", ")})")
      file.add(s"""case x => throw new IllegalStateException("Unable to find newly-inserted ${model.title}.")""")
    }
    file.add("}", -1)

    file.addImport("models.result.data", "DataField")
    file.add(s"def create(fields: Seq[DataField]) = Database.execute(${model.className}Queries.create(fields)).map { _ =>", 1)
    file.add(s"None: Option[${model.className}]")
    file.add("}", -1)
    file.add()

    if (model.pkFields.nonEmpty) {
      model.pkFields.foreach(f => f.t.requiredImport.foreach(x => file.addImport(x, f.t.asScala)))
      val sig = model.pkFields.map(f => f.propertyName + ": " + f.t.asScala).mkString(", ")
      val call = model.pkFields.map(_.propertyName).mkString(", ")
      val interp = model.pkFields.map(f => "$" + f.propertyName).mkString(", ")
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
    model.pkFields.foreach(field => field.t.requiredImport.foreach(pkg => file.addImport(pkg, field.t.asScala)))
    model.pkFields match {
      case Nil => // noop
      case field :: Nil =>
        val colProp = field.propertyName
        file.add(s"def getByPrimaryKey($colProp: ${field.t.asScala}) = Database.query(${model.className}Queries.getByPrimaryKey($colProp))")
        val seqArgs = s"${colProp}Seq: Seq[${field.t.asScala}]"
        file.add(s"def getByPrimaryKeySeq($seqArgs) = Database.query(${model.className}Queries.getByPrimaryKeySeq(${colProp}Seq))")
        field.t match {
          case ColumnType.UuidType => file.addMarker("uuid-search", InjectSearchParams(model).toString)
          case ColumnType.IntegerType => file.addMarker("int-search", InjectSearchParams(model).toString)
          case _ => // noop
        }
      case fields => // multiple columns
        val tupleTyp = "(" + fields.map(_.t.asScala).mkString(", ") + ")"
        val colArgs = fields.map(f => f.propertyName + ": " + f.t.asScala).mkString(", ")
        val queryArgs = fields.map(_.propertyName).mkString(", ")
        file.add(s"def getByPrimaryKey($colArgs) = Database.query(${model.className}Queries.getByPrimaryKey($queryArgs))")
        file.add(s"def getByPrimaryKeySeq(pkSeq: Seq[$tupleTyp]) = Database.query(${model.className}Queries.getByPrimaryKeySeq(pkSeq))")
    }
    file.add()
  }
}
