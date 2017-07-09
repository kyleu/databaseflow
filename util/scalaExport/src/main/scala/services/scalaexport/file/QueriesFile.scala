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
      case pkCol :: Nil =>
        val name = ExportHelper.toIdentifier(pkCol.name)
        pkCol.columnType.requiredImport.foreach(x => file.addImport(x, pkCol.columnType.asScala))
        file.add(s"def getById($name: ${pkCol.columnType.asScala}) = GetById(Seq($name))")
        file.add(s"""def getByIdSeq(${name}Seq: Seq[${pkCol.columnType.asScala}]) = new ColSeqQuery("${pkCol.name}", ${name}Seq)""")
        file.add()
      case _ => // multiple columns
    }
    file.add(s"def getAll(${ExportHelper.getAllArgs}) = GetAll(orderBy, limit, offset)")
    file.add()

    ForeignKeysFile.writeQueries(et, file)

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

    file.add(s"override protected def fromRow(row: Row) = ${et.className}(", 1)
    et.t.columns.foreach { col =>
      col.columnType.requiredImport.foreach { p =>
        file.addImport(p, col.columnType.asScala)
      }
      val comma = if (et.t.columns.lastOption.contains(col)) { "" } else { "," }
      val asType = if (col.notNull) { s"as[${col.columnType.asScala}]" } else { s"asOpt[${col.columnType.asScala}]" }
      file.add(s"""${ExportHelper.toIdentifier(col.name)} = row.$asType("${col.name}")$comma""")
    }
    file.add(")", -1)
    file.add(s"override protected def toDataSeq(model: ${et.className}) = model.productIterator.toSeq")

    file.add("}", -1)
    file
  }
}
