package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.Table

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

    file.add("}", -1)
    file.filename -> file.render()
  }
}
