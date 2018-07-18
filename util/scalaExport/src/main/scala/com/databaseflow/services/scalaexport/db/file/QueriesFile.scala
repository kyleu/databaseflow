package com.databaseflow.services.scalaexport.db.file

import com.databaseflow.models.scalaexport.db.ExportModel
import com.databaseflow.models.scalaexport.db.config.ExportConfiguration
import com.databaseflow.models.scalaexport.file.ScalaFile

object QueriesFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(pkg = model.queriesPackage, key = model.className + "Queries", core = true)

    file.addImport(model.modelPackage.mkString("."), model.className)
    file.addImport(config.providedPrefix + "models.database", "Row")
    file.addImport(config.providedPrefix + "models.database", "DatabaseField")
    file.addImport(config.providedPrefix + "models.database.DatabaseFieldType", "_")

    if (model.pkg.nonEmpty) {
      file.addImport(config.providedPrefix + "models.queries", "BaseQueries")
    }

    file.add(s"""object ${model.className}Queries extends BaseQueries[${model.className}]("${model.propertyName}", "${model.tableName}") {""", 1)
    file.add("override val fields = Seq(", 1)
    model.fields.foreach { f =>
      f.addImport(file)
      val field = s"""DatabaseField(title = "${f.title}", prop = "${f.propertyName}", col = "${f.columnName}", typ = ${f.classNameForSqlType})"""
      val comma = if (model.fields.lastOption.contains(f)) { "" } else { "," }
      file.add(field + comma)
    }
    file.add(")", -1)

    if (model.pkFields.nonEmpty) {
      file.add("override protected val pkColumns = Seq(" + model.pkFields.map("\"" + _.columnName + "\"").mkString(", ") + ")")
      file.add(s"override protected val searchColumns = Seq(${model.searchFields.map("\"" + _.columnName + "\"").mkString(", ")})")
    }
    file.add()

    file.addImport(config.providedPrefix + "models.result.filter", "Filter")
    file.add("def countAll(filters: Seq[Filter] = Nil) = onCountAll(filters)")

    file.add("def getAll(filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) = {", 1)
    file.add("new GetAll(filters, orderBys, limit, offset)")
    file.add("}", -1)
    file.add()

    val searchArgs = "q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None"
    file.add(s"def search($searchArgs) = {", 1)
    file.add("new Search(q, filters, orderBys, limit, offset)")
    file.add("}", -1)
    file.add("def searchCount(q: Option[String], filters: Seq[Filter] = Nil) = new SearchCount(q, filters)")
    file.add("def searchExact(q: String, orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]) = new SearchExact(q, orderBys, limit, offset)")
    file.add()

    writePkFields(file, model)

    QueriesHelper.writeForeignKeys(config.providedPrefix, model, file)

    if (!model.readOnly) {
      file.add(s"def insert(model: ${model.className}) = new Insert(model)")
      file.add(s"def insertBatch(models: Seq[${model.className}]) = new InsertBatch(models)")

      file.addImport(config.providedPrefix + "models.result.data", "DataField")
      file.add("def create(dataFields: Seq[DataField]) = new CreateFields(dataFields)")
    }

    if (model.pkFields.nonEmpty) {
      val sig = model.pkFields.map(f => f.propertyName + ": " + f.scalaType).mkString(", ")
      val call = model.pkFields.map(_.propertyName).mkString(", ")
      file.add()
      file.add(s"def removeByPrimaryKey($sig) = new RemoveByPrimaryKey(Seq[Any]($call))")
      file.add()
      file.add(s"def update($sig, fields: Seq[DataField]) = new UpdateFields(Seq[Any]($call), fields)")
    }

    file.add()
    QueriesHelper.fromRow(model, file)

    file.add("}", -1)
    file
  }

  private[this] def writePkFields(file: ScalaFile, model: ExportModel) = model.pkFields match {
    case Nil => // noop
    case pkField :: Nil =>
      val name = pkField.propertyName
      pkField.addImport(file)
      file.add(s"def getByPrimaryKey($name: ${model.pkType}) = new GetByPrimaryKey(Seq($name))")
      file.add(s"""def getByPrimaryKeySeq(${name}Seq: Seq[${model.pkType}]) = new ColSeqQuery(column = "${pkField.columnName}", values = ${name}Seq)""")
      file.add()
    case pkFields =>
      pkFields.foreach(_.addImport(file))
      val args = pkFields.map(x => s"${x.propertyName}: ${x.scalaType}").mkString(", ")
      val seqArgs = pkFields.map(_.propertyName).mkString(", ")
      file.add(s"def getByPrimaryKey($args) = new GetByPrimaryKey(Seq[Any]($seqArgs))")
      file.add(s"def getByPrimaryKeySeq(idSeq: Seq[${model.pkType}]) = new SeqQuery(", 1)
      val pkWhere = pkFields.map(f => "\\\"" + f.columnName + "\\\" = ?").mkString(" and ")
      file.add(s"""whereClause = Some(idSeq.map(_ => "($pkWhere)").mkString(" or ")),""")
      file.add("values = idSeq.flatMap(_.productIterator.toSeq)")
      file.add(")", -1)
      file.add()
  }
}
