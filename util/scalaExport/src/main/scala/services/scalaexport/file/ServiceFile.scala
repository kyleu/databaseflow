package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.{ExportHelper, ExportTable}

object ServiceFile {
  def export(et: ExportTable) = {
    val file = ScalaFile("services" +: et.pkg, et.className + "Service")
    file.addImport(("models" +: et.pkg).mkString("."), et.className)
    file.addImport(("models" +: "queries" +: et.pkg).mkString("."), et.className + "Queries")
    file.addImport(("services" :: "database" :: Nil).mkString("."), "Database")
    file.add(s"object ${et.className}Service {", 1)

    et.pkColumns match {
      case Nil => // noop
      case col :: Nil =>
        col.columnType.requiredImport.foreach(pkg => file.addImport(pkg, col.columnType.asScala))
        val colProp = ExportHelper.toIdentifier(col.name)
        file.add(s"def getById($colProp: ${col.columnType.asScala}) = Database.query(${et.className}Queries.getById($colProp))")
        file.add(s"def getByIdSeq(${colProp}Seq: Seq[${col.columnType.asScala}]) = Database.query(${et.className}Queries.getByIdSeq(${colProp}Seq))")
      case cols => // multiple columns
        cols.foreach(col => col.columnType.requiredImport.foreach(pkg => file.addImport(pkg, col.columnType.asScala)))
        val colTyp = "(" + cols.map(_.columnType.asScala).mkString(", ") + ")"
        file.add(s"def getById(id: $colTyp) = Database.query(${et.className}Queries.getById(id))")
        file.add(s"def getByIdSeq(idSeq: Seq[$colTyp]) = Database.query(${et.className}Queries.getByIdSeq(idSeq))")
    }
    file.add()

    file.add(s"def getAll(${ExportHelper.getAllArgs}) = {", 1)
    file.add(s"Database.query(${et.className}Queries.getAll(orderBy, limit, offset))")
    file.add("}", -1)
    file.add()

    ForeignKeysFile.writeService(et, file)

    file.add(s"def searchCount(q: String) = Database.query(${et.className}Queries.searchCount(q))")
    file.add(s"def search(${ExportHelper.searchArgs}) = {", 1)
    file.add(s"Database.query(${et.className}Queries.search(q, orderBy, limit, offset))")
    file.add("}", -1)
    file.add()
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
