package services.scalaexport.file

import models.scalaexport.ScalaFile
import models.schema.Table
import services.scalaexport.ExportHelper

object QueriesFile {
  def export(className: String, pkg: Seq[String], t: Table) = {
    val file = ScalaFile("models" +: "queries" +: pkg, className + "Queries")

    file.addImport(("models" +: pkg).mkString("."), className)
    file.addImport("models.database", "Row")

    if (pkg.nonEmpty) {
      file.addImport("models.queries", "BaseQueries")
    }

    file.add(s"object ${className}Queries extends BaseQueries[$className] {", 1)
    file.add(s"""override protected val tableName = "${t.name}"""")
    file.add("override protected val columns = Seq(" + t.columns.map("\"" + _.name + "\"").mkString(", ") + ")")
    t.primaryKey.map { pk =>
      file.add("override protected def idColumns = Seq(" + pk.columns.map("\"" + _ + "\"").mkString(", ") + ")")
    }
    file.add()
    t.primaryKey.map { pk =>
      pk.columns.toList match {
        case Nil => // noop
        case h :: Nil =>
          val pkCol = t.columns.find(_.name == h).getOrElse {
            throw new IllegalStateException(s"Cannot derive primary key for [${t.name}] with key [${t.primaryKey}].")
          }
          pkCol.columnType.requiredImport.foreach(x => file.addImport(x, pkCol.columnType.asScala))
          file.add(s"def getById(${pkCol.name}: ${pkCol.columnType.asScala}) = GetById(Seq(${pkCol.name}))")
          file.add(s"// def getByIds(${pkCol.name}: Seq[${pkCol.columnType.asScala}]) = GetByIds(${pkCol.name})")
          file.add()
        case _ => // multiple columns
      }
    }

    file.add(s"override protected def fromRow(row: Row) = $className(", 1)
    t.columns.foreach { col =>
      col.columnType.requiredImport.foreach { p =>
        file.addImport(p, col.columnType.asScala)
      }

      val comma = if (t.columns.lastOption.contains(col)) { "" } else { "," }
      val propName = ExportHelper.toScalaIdentifier.convert(col.name)
      val asType = if (col.notNull) { s"as[${col.columnType.asScala}]" } else { s"asOpt[${col.columnType.asScala}]" }
      file.add(s"""$propName = row.$asType("${col.name}")$comma""")
    }
    file.add(")", -1)
    file.add(s"override protected def toDataSeq(model: $className) = model.productIterator.toSeq")

    file.add("}", -1)
    ("models" +: "queries" +: pkg, file.filename, file.render())
  }
}
