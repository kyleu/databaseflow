package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.Table
import services.scalaexport.ExportHelper

object QueriesFile {
  def export(className: String, t: Table) = {
    val file = ScalaFile("models.queries", className + "Queries")

    file.addImport("models.queries", "BaseQueries")

    file.add(s"object ${className}Queries extends BaseQueries[$className] {", 1)
    file.add(s"""override protected val tableName = "${t.name}"""")
    file.add("override protected val columns = Seq(" + t.columns.map("\"" + _.name + "\"").mkString(", ") + ")")
    t.primaryKey.map { pk =>
      file.add("override protected def idColumns = Seq(" + pk.columns.map("\"" + _ + "\"").mkString(", ") + ")")
    }
    file.add()
    file.add(s"override protected def fromRow(row: Row) = $className(", 1)
    t.columns.foreach { col =>
      val comma = if (t.columns.lastOption.contains(col)) { "" } else { "," }
      val propName = ExportHelper.toScalaIdentifier.convert(col.name)
      val asType = if (col.notNull) { s"as[${col.columnType.asScala}]" } else { s"asOpt[${col.columnType.asScala}]" }
      file.add(s"""$propName = row.$asType("${col.name}")$comma""")
    }
    file.add(")", -1)
    file.add(s"override protected def toDataSeq(model: $className) = model.productIterator.toSeq")

    file.add("}", -1)
    file.filename -> file.render()
  }
}
