package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportConfiguration, ExportModel}

object ForeignKeysHelper {
  def writeQueries(model: ExportModel, file: ScalaFile) = model.foreignKeys.foreach { fk =>
    fk.references.toList match {
      case h :: Nil =>
        val col = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
        col.t.requiredImport.foreach(pkg => file.addImport(pkg, col.t.asScala))
        val idType = if (col.notNull) { col.t.asScala } else { "Option[" + col.t.asScala + "]" }
        val propId = col.propertyName
        val propCls = col.className
        file.add(s"""case class GetBy$propCls($propId: $idType) extends SeqQuery("where \\"${h.source}\\" = ?", Seq($propId))""")
        file.add(s"""case class GetBy${propCls}Seq(${propId}Seq: Seq[$idType]) extends ColSeqQuery("${h.source}", ${propId}Seq)""")
        file.add()
      case _ => // noop
    }
  }

  def writeService(model: ExportModel, file: ScalaFile) = model.foreignKeys.foreach { fk =>
    fk.references.toList match {
      case h :: Nil =>
        val col = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
        val idType = if (col.notNull) { col.t.asScala } else { "Option[" + col.t.asScala + "]" }
        col.t.requiredImport.foreach(pkg => file.addImport(pkg, col.t.asScala))
        val propId = col.propertyName
        val propCls = col.className
        file.add(s"""def getBy$propCls($propId: $idType)(implicit trace: TraceData) = Database.query(${model.className}Queries.GetBy$propCls($propId))""")
        file.add(s"""def getBy${propCls}Seq(${propId}Seq: Seq[$idType])(implicit trace: TraceData) = Database.query(${model.className}Queries.GetBy${propCls}Seq(${propId}Seq))""")
        file.add()
      case _ => // noop
    }
  }

  def writeSchema(config: ExportConfiguration, src: ExportModel, file: ScalaFile) = if (src.foreignKeys.nonEmpty) {
    file.addImport("sangria.execution.deferred", "Fetcher")
    val fks = src.foreignKeys.filter(_.references.size == 1)
    fks.foreach { fk =>
      fk.references.toList match {
        case Nil => // noop
        case h :: Nil => config.getModelOpt(fk.targetTable).foreach({ tgt =>
          val srcCol = src.getField(h.source)
          val tgtCol = tgt.getField(h.target)
          val idType = if (srcCol.notNull) { srcCol.t.asScala } else { "Option[" + srcCol.t.asScala + "]" }
          srcCol.t.requiredImport.foreach(pkg => file.addImport(pkg, srcCol.t.asScala))
          file.addImport("sangria.execution.deferred", "HasId")
          val fn = s"${src.propertyName}By${srcCol.className}Fetcher"
          file.addMarker("fetcher", (src.modelPackage :+ s"${src.className}Schema" :+ fn).mkString("."))
          file.add(s"val $fn = Fetcher { (c: GraphQLContext, values: Seq[$idType]) =>", 1)
          file.add(s"c.${src.serviceReference}.getBy${srcCol.className}Seq(values)(c.trace)")
          file.add(s"}(HasId[${src.className}, $idType](_.${srcCol.propertyName}))", -1)
          file.add()
        })
        case _ => // noop
      }
    }
  }
}
