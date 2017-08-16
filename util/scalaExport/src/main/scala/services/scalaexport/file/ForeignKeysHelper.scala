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
        file.add(s"""def getBy$propCls($propId: $idType) = Database.query(${model.className}Queries.GetBy$propCls($propId))""")
        file.add(s"""def getBy${propCls}Seq(${propId}Seq: Seq[$idType]) = Database.query(${model.className}Queries.GetBy${propCls}Seq(${propId}Seq))""")
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
        case h :: Nil =>
          val tgt = config.getModel(fk.targetTable)
          val srcCol = src.getField(h.source)
          val tgtCol = tgt.getField(h.target)
          val idType = if (srcCol.notNull) { srcCol.t.asScala } else { "Option[" + srcCol.t.asScala + "]" }
          srcCol.t.requiredImport.foreach(pkg => file.addImport(pkg, srcCol.t.asScala))
          file.addImport("sangria.execution.deferred", "HasId")
          val fn = s"${src.propertyName}By${srcCol.className}Fetcher"
          file.addMarker("fetcher", (src.modelPackage :+ s"${src.className}Schema" :+ fn).mkString("."))
          file.add(s"val $fn = Fetcher { (c: GraphQLContext, values: Seq[$idType]) =>", 1)
          file.add(s"${src.className}Service.getBy${srcCol.className}Seq(values)")
          file.add(s"}(HasId[${src.className}, $idType](_.${srcCol.propertyName}))", -1)
          file.add()
        case _ => // noop
      }
    }
  }

  def writeFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = if (model.foreignKeys.nonEmpty) {
    val fks = model.foreignKeys.filter(_.references.size == 1)
    fks.foreach { fk =>
      config.getModelOpt(fk.targetTable).foreach { targetTable =>
        val fkCols = fk.references
        val fields = fkCols.map(h => model.fields.find(_.columnName == h.source).getOrElse {
          throw new IllegalStateException(s"Missing source column [${h.source}].")
        })
        val targets = fkCols.map(h => targetTable.fields.find(_.columnName == h.target).getOrElse {
          throw new IllegalStateException(s"Missing target column [${h.target}].")
        })
        fields.foreach(f => f.t.requiredImport.foreach(pkg => file.addImport(pkg, f.t.asScala)))
        if (targetTable.pkg != model.pkg) { file.addImport(targetTable.modelPackage.mkString("."), targetTable.className + "Schema") }

        file.add("Field(", 1)
        file.add(s"""name = "${fields.map(_.propertyName).mkString}Rel",""")

        fields.toList match {
          case field :: Nil =>
            if (field.notNull) {
              file.add(s"""fieldType = ${targetTable.className}Schema.${targetTable.propertyName}Type,""")
            } else {
              file.add(s"""fieldType = OptionType(${targetTable.className}Schema.${targetTable.propertyName}Type),""")
            }

            val fetcherRef = if (targetTable.pkFields.map(_.propertyName) == targets.map(_.propertyName)) {
              s"${targetTable.className}Schema.${targetTable.propertyName}ByPrimaryKeyFetcher"
            } else {
              s"${targetTable.className}Schema.${targetTable.propertyName}By${targets.map(_.className).mkString}Fetcher"
            }
            if (field.notNull) {
              file.add(s"resolve = ctx => $fetcherRef.defer(ctx.value.${field.propertyName})")
            } else {
              file.add(s"resolve = ctx => $fetcherRef.deferOpt(ctx.value.${field.propertyName})")
            }
            val comma = if (fks.lastOption.contains(fk)) { "" } else { "," }
            file.add(")" + comma, -1)
          case _ => throw new IllegalStateException(s"Unhandled foreign key references [${fields.map(_.propertyName).mkString(", ")}].")
        }
      }
    }
  }
}
