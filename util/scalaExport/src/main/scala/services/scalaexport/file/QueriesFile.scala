package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportEngine, ExportModel}

object QueriesFile {
  def export(engine: ExportEngine, model: ExportModel) = {
    val file = ScalaFile(model.queriesPackage, model.className + "Queries")

    file.addImport(model.modelPackage.mkString("."), model.className)
    file.addImport("models.database", "Row")
    file.addImport("models.database", "DatabaseField")

    if (model.pkg.nonEmpty) {
      file.addImport("models.queries", "BaseQueries")
    }

    file.add(s"object ${model.className}Queries extends BaseQueries[${model.className}] {", 1)
    file.add(s"""override val tableName = "${model.tableName}"""")
    file.add("override val fields = Seq(", 1)
    model.fields.foreach { f =>
      val field = s"""DatabaseField(title = "${f.title}", prop = "${f.propertyName}", col = "${f.columnName}", typ = "string")"""
      val comma = if (model.fields.lastOption.contains(f)) { "" } else { "," }
      file.add(field + comma)
    }
    file.add(")", -1)

    if (model.pkFields.nonEmpty) {
      file.add("override protected val pkColumns = Seq(" + model.pkFields.map("\"" + _.columnName + "\"").mkString(", ") + ")")
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

    model.pkFields match {
      case Nil => // noop
      case pkField :: Nil =>
        val name = pkField.propertyName
        pkField.t.requiredImport.foreach(x => file.addImport(x, pkField.t.asScala))
        file.add(s"def getByPrimaryKey($name: ${SchemaHelper.pkType(model.pkFields)}) = GetByPrimaryKey(Seq($name))")
        file.add(s"""def getByPrimaryKeySeq(${name}Seq: Seq[${SchemaHelper.pkType(model.pkFields)}]) = new ColSeqQuery("${pkField.columnName}", ${name}Seq)""")
        file.add()
      case pkFields =>
        pkFields.foreach(pkField => pkField.t.requiredImport.foreach(x => file.addImport(x, pkField.t.asScala)))
        val args = pkFields.map(x => s"${x.propertyName}: ${x.t.asScala}").mkString(", ")
        val seqArgs = pkFields.map(_.propertyName).mkString(", ")
        file.add(s"def getByPrimaryKey($args) = GetByPrimaryKey(Seq[Any]($seqArgs))")
        file.add(s"def getByPrimaryKeySeq(idSeq: Seq[${SchemaHelper.pkType(model.pkFields)}]) = new SeqQuery(", 1)
        val pkWhere = pkFields.map(f => s"""\\"${f.columnName}\\" = ?""").mkString(" and ")
        file.add(s"""additionalSql = " where " + idSeq.map(_ => "($pkWhere)").mkString(" or "),""")
        file.add("values = idSeq.flatMap(_.productIterator.toSeq)")
        file.add(")", -1)
        file.add()
    }

    ForeignKeysHelper.writeQueries(model, file)

    file.add(s"def insert(model: ${model.className}) = Insert(model)")

    file.addImport("models.result.data", "DataField")
    file.add("def create(fields: Seq[DataField]) = CreateFields(fields)")

    if (model.pkFields.nonEmpty) {
      val sig = model.pkFields.map(f => f.propertyName + ": " + f.t.asScala).mkString(", ")
      val call = model.pkFields.map(_.propertyName).mkString(", ")
      file.add(s"def removeByPrimaryKey($sig) = RemoveByPrimaryKey(Seq[Any]($call))")
      file.add()
      file.add(s"def update($sig, fields: Seq[DataField]) = UpdateFields(Seq[Any]($call), fields)")
    }

    QueriesHelper.fromRow(engine, model, file)
    QueriesHelper.toDataSeq(engine, model, file)

    file.add("}", -1)
    file
  }
}
