package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.{ExportHelper, ExportTable}

object QueriesFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("models" +: "queries" +: et.pkg, et.className + "Queries")

    file.addImport(("models" +: et.pkg).mkString("."), et.className)
    file.addImport("models.database", "Row")

    if (et.pkg.nonEmpty) {
      file.addImport("models.queries", "BaseQueries")
    }

    file.add(s"object ${et.className}Queries extends BaseQueries[${et.className}] {", 1)
    file.add(s"""override protected val tableName = "${et.t.name}"""")

    file.addImport("models.database", "DatabaseField")
    file.add("override protected val fields = Seq(", 1)
    et.t.columns.foreach { c =>
      val field = s"""DatabaseField(title = "${ExportHelper.toClassName(c.name)}", prop = "${ExportHelper.toIdentifier(c.name)}", col = "${c.name}", typ = "string")"""
      val comma = if (et.t.columns.lastOption.contains(c)) { "" } else { "," }
      file.add(field + comma)
    }
    file.add(")", -1)

    et.t.primaryKey.foreach { pk =>
      file.add("override protected val idColumns = Seq(" + pk.columns.map("\"" + _ + "\"").mkString(", ") + ")")
      val searchColumns = et.config.searchColumns.getOrElse(ExportHelper.toIdentifier(et.t.name), pk.columns.map(x => x -> x))
      file.add(s"override protected val searchColumns = Seq(${searchColumns.map("\"" + _._1 + "\"").mkString(", ")})")
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

    et.pkColumns match {
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
        file.add(s"def getByIdSeq(idSeq: Seq[${et.pkType.getOrElse("String")}]) = new SeqQuery(", 1)
        file.add(s"""additionalSql = " where " + idSeq.map(_ => "(${pkCols.map(c => s"""\\"${c.name}\\" = ?""").mkString(" and ")})").mkString(" or "),""")
        file.add("values = idSeq.flatMap(_.productIterator.toSeq)")
        file.add(")", -1)
        file.add()
    }

    ForeignKeysHelper.writeQueries(et, file)

    file.add(s"def insert(model: ${et.className}) = Insert(model)")
    et.pkColumns match {
      case Nil => // noop
      case pkCol :: Nil =>
        val name = ExportHelper.toIdentifier(pkCol.name)
        file.add(s"def removeById($name: ${pkCol.columnType.asScala}) = RemoveById(Seq($name))")
      case _ => // multiple columns
    }
    file.add()

    QueriesHelper.fromRow(et, file)
    QueriesHelper.toDataSeq(et, file)

    file.add("}", -1)
    file
  }
}
