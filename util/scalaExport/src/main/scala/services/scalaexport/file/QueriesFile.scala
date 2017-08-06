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
    file.add("override protected val columns = Seq(" + et.t.columns.map("\"" + _.name + "\"").mkString(", ") + ")")
    et.t.primaryKey.foreach { pk =>
      file.add("override protected val idColumns = Seq(" + pk.columns.map("\"" + _ + "\"").mkString(", ") + ")")
      val searchColumns = et.config.searchColumns.getOrElse(ExportHelper.toIdentifier(et.t.name), pk.columns)
      file.add(s"override protected val searchColumns = Seq(${searchColumns.map("\"" + _ + "\"").mkString(", ")})")
    }
    file.add()
    et.pkColumns match {
      case Nil => // noop
      case pkCols =>
        val args = pkCols.map(x => s"${ExportHelper.toIdentifier(x.name)}: ${x.columnType.asScalaFull}").mkString(", ")
        val seqArgs = pkCols.map(x => ExportHelper.toIdentifier(x.name)).mkString(", ")
        file.add(s"def getById($args) = GetById(Seq($seqArgs))")
        file.add(s"""def getByIdSeq(idSeq: Seq[${et.pkType.getOrElse("String")}]) = new SeqQuery("??? TODO ???", idSeq)""")
        file.add()
    }
    file.add(s"def getAll(${ExportHelper.getAllArgs}) = GetAll(orderBy, limit, offset)")
    file.add()

    ForeignKeysHelper.writeQueries(et, file)

    file.add(s"def search(${ExportHelper.searchArgs}) = Search(q, orderBy, limit, offset)")
    file.add("def searchCount(q: String) = SearchCount(q)")
    file.add()

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
