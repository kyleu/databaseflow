package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.{ExportHelper, ExportTable}

object ForeignKeysFile {
  def writeQueries(et: ExportTable, file: ScalaFile) = et.t.foreignKeys.foreach { fk =>
    fk.references.toList match {
      case h :: Nil =>
        val col = et.t.columns.find(_.name == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
        val typ = col.columnType.asScala
        val propId = ExportHelper.toIdentifier(h.source)
        val propCls = ExportHelper.toClassName(h.source)
        file.add(s"""case class GetBy$propCls($propId: $typ) extends SeqQuery("where \\"${h.source}\\" = ?", Seq($propId))""")
        file.add(s"""case class GetBy${propCls}Seq(${propId}Seq: Seq[$typ]) extends ColSeqQuery("${h.source}", ${propId}Seq)""")
        file.add()
      case _ => // noop
    }
  }

  def writeService(et: ExportTable, file: ScalaFile) = et.t.foreignKeys.foreach { fk =>
    fk.references.toList match {
      case h :: Nil =>
        val col = et.t.columns.find(_.name == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
        val typ = col.columnType.asScala
        val propId = ExportHelper.toIdentifier(h.source)
        val propCls = ExportHelper.toClassName(h.source)
        file.add(s"""def getBy$propCls($propId: $typ) = Database.query(${et.className}Queries.GetBy$propCls($propId))""")
        file.add(s"""def getBy${propCls}Seq(${propId}Seq: Seq[$typ]) = Database.query(${et.className}Queries.GetBy${propCls}Seq(${propId}Seq))""")
        file.add()
      case _ => // noop
    }
  }

  def writeSchema(et: ExportTable, file: ScalaFile) = if (et.t.foreignKeys.size > 1) {
    file.addImport("sangria.execution.deferred", "Relation")

    et.t.foreignKeys.foreach { fk =>
      val targetTable = et.s.getTable(fk.targetTable).getOrElse(throw new IllegalStateException(s"Missing table [${fk.targetTable}]."))
      val tgtClassName = ExportHelper.toClassName(targetTable.name)
      fk.references.toList match {
        case h :: Nil =>
          val col = et.t.columns.find(_.name == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
          val typ = if (col.notNull) { col.columnType.asScala } else { s"Option[${col.columnType.asScala}]" }

          val propName = ExportHelper.toIdentifier(h.source)
          file.add(s"""val ${et.propertyName}By$tgtClassName = Relation[${et.className}, $typ]("by$tgtClassName", x => Seq(x.$propName))""")
        case _ => // noop
      }
    }
    file.add()
  }
}
