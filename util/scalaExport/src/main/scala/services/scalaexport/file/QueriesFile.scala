package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportModel

object QueriesFile {
  def export(model: ExportModel) = {
    val file = ScalaFile("models" +: "queries" +: model.pkg, model.className + "Queries")

    file.addImport(("models" +: model.pkg).mkString("."), model.className)
    file.addImport("models.database", "Row")

    if (model.pkg.nonEmpty) {
      file.addImport("models.queries", "BaseQueries")
    }

    file.add(s"object ${model.className}Queries extends BaseQueries[${model.className}] {", 1)
    file.add(s"""override protected val tableName = "${model.tableName}"""")

    file.addImport("models.database", "DatabaseField")
    file.add("override protected val fields = Seq(", 1)
    model.fields.foreach { f =>
      val field = s"""DatabaseField(title = "${f.title}", prop = "${f.propertyName}", col = "${f.columnName}", typ = "string")"""
      val comma = if (model.fields.lastOption.contains(f)) { "" } else { "," }
      file.add(field + comma)
    }
    file.add(")", -1)

    if (model.pkColumns.nonEmpty) {
      file.add("override protected val idColumns = Seq(" + model.pkColumns.map("\"" + _.name + "\"").mkString(", ") + ")")
      val searchColumns = model.fields.filter(_.inSearch)
      file.add(s"override protected val searchColumns = Seq(${searchColumns.map("\"" + _.columnName + "\"").mkString(", ")})")
    }
    file.add()

    file.addImport("models.result.filter", "Filter")
    file.add("def countAll(filters: Seq[Filter] = Nil) = onCountAll(filters)")
    file.add("def getAll = GetAll")
    file.add()
    file.add("val search = Search")
    file.add("val searchCount = SearchCount")
    file.add("val searchExact = SearchExact")
    file.add()

    model.pkColumns match {
      case Nil => // noop
      case pkCol :: Nil =>
        val name = ExportHelper.toIdentifier(pkCol.name)
        pkCol.columnType.requiredImport.foreach(x => file.addImport(x, pkCol.columnType.asScala))
        file.add(s"def getById($name: ${pkCol.columnType.asScala}) = GetById(Seq($name))")
        file.add(s"""def getByIdSeq(${name}Seq: Seq[${pkCol.columnType.asScala}]) = new ColSeqQuery("${pkCol.name}", ${name}Seq)""")
        file.add()
      case pkCols =>
        pkCols.foreach(pkCol => pkCol.columnType.requiredImport.foreach(x => file.addImport(x, pkCol.columnType.asScala)))
        val args = pkCols.map(x => s"${ExportHelper.toIdentifier(x.name)}: ${x.columnType.asScalaFull}").mkString(", ")
        val seqArgs = pkCols.map(x => ExportHelper.toIdentifier(x.name)).mkString(", ")
        file.add(s"def getById($args) = GetById(Seq($seqArgs))")
        file.add(s"def getByIdSeq(idSeq: Seq[${SchemaHelper.pkType(model.pkColumns)}]) = new SeqQuery(", 1)
        file.add(s"""additionalSql = " where " + idSeq.map(_ => "(${pkCols.map(c => s"""\\"${c.name}\\" = ?""").mkString(" and ")})").mkString(" or "),""")
        file.add("values = idSeq.flatMap(_.productIterator.toSeq)")
        file.add(")", -1)
        file.add()
    }

    ForeignKeysHelper.writeQueries(model, file)

    file.add(s"def insert(model: ${model.className}) = Insert(model)")
    if (model.pkColumns.nonEmpty) {
      file.addImport("models.result.data", "DataField")
      val sig = model.pkColumns.map(c => ExportHelper.toIdentifier(c.name) + ": " + c.columnType.asScala).mkString(", ")
      val call = model.pkColumns.map(c => ExportHelper.toIdentifier(c.name)).mkString(", ")
      file.add(s"def removeById($sig) = RemoveById(Seq($call))")
      file.add()
      file.add("def create(fields: Seq[DataField]) = CreateFields(fields)")
      file.add(s"def update($sig, fields: Seq[DataField]) = UpdateFields(Seq($call), fields)")
    }

    QueriesHelper.fromRow(model, file)
    QueriesHelper.toDataSeq(model, file)

    file.add("}", -1)
    file
  }
}
