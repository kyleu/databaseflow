package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.ExportHelper
import services.scalaexport.config.{ExportConfiguration, ExportModel}

object ForeignKeysHelper {
  def writeQueries(model: ExportModel, file: ScalaFile) = model.foreignKeys.foreach { fk =>
    fk.references.toList match {
      case h :: Nil =>
        val col = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
        val typ = col.t.asScala
        val propId = ExportHelper.toIdentifier(h.source)
        val propCls = ExportHelper.toClassName(h.source)
        file.add(s"""case class GetBy$propCls($propId: $typ) extends SeqQuery("where \\"${h.source}\\" = ?", Seq($propId))""")
        file.add(s"""case class GetBy${propCls}Seq(${propId}Seq: Seq[$typ]) extends ColSeqQuery("${h.source}", ${propId}Seq)""")
        file.add()
      case _ => // noop
    }
  }

  def writeService(model: ExportModel, file: ScalaFile) = model.foreignKeys.foreach { fk =>
    fk.references.toList match {
      case h :: Nil =>
        val col = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
        val typ = col.t.asScala
        col.t.requiredImport.foreach(pkg => file.addImport(pkg, typ))
        val propId = ExportHelper.toIdentifier(h.source)
        val propCls = ExportHelper.toClassName(h.source)
        file.add(s"""def getBy$propCls($propId: $typ) = Database.query(${model.className}Queries.GetBy$propCls($propId))""")
        file.add(s"""def getBy${propCls}Seq(${propId}Seq: Seq[$typ]) = Database.query(${model.className}Queries.GetBy${propCls}Seq(${propId}Seq))""")
        file.add()
      case _ => // noop
    }
  }

  def writeSchema(model: ExportModel, file: ScalaFile) = if (model.foreignKeys.nonEmpty) {
    file.addImport("sangria.execution.deferred", "Relation")
    file.addImport("sangria.execution.deferred", "Fetcher")

    model.foreignKeys.foreach { fk =>
      fk.references.toList match {
        case h :: Nil =>
          val col = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
          val typ = col.t.asScala
          col.t.requiredImport.foreach(pkg => file.addImport(pkg, typ))
          val propName = col.propertyName
          val srcClass = col.className
          val seq = if (col.notNull) { s"Seq(x.$propName)" } else { s"x.$propName.toSeq" }
          file.addMarker("fetcher", (file.pkg :+ s"${model.className}Schema" :+ s"${model.propertyName}By${srcClass}Fetcher").mkString("."))
          file.add(s"""val ${model.propertyName}By$srcClass = Relation[${model.className}, $typ]("by$srcClass", x => $seq)""")
          val relType = s"GraphQLContext, ${model.className}, ${model.className}, ${SchemaHelper.pkType(model.pkColumns).getOrElse("String")}"
          file.add(s"val ${model.propertyName}By${srcClass}Fetcher = Fetcher.rel[$relType](", 1)
          file.add(s"(_, ids) => ${model.className}Service.getByIdSeq(ids),")
          file.add(s"(_, rels) => ${model.className}Service.getBy${srcClass}Seq(rels(${model.propertyName}By$srcClass))")
          file.add(")", -1)

          file.add()
        case _ => // noop
      }
    }
  }

  def writeFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = if (model.foreignKeys.nonEmpty) {
    model.foreignKeys.foreach { fk =>
      val targetTable = config.getModel(fk.targetTable)
      fk.references.toList match {
        case h :: Nil =>
          val field = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
          val typ = field.t.asScala
          field.t.requiredImport.foreach(pkg => file.addImport(pkg, typ))
          val tgtClass = targetTable.className
          val tgtProp = targetTable.propertyName
          val srcProp = field.propertyName
          val p = targetTable.propertyName
          val cls = targetTable.className
          val pkg = targetTable.pkg
          if (pkg != model.pkg) { file.addImport(("models" +: pkg).mkString("."), cls + "Schema") }
          file.add("Field(", 1)
          file.add(s"""name = "${srcProp}Rel",""")
          if (field.notNull) {
            file.add(s"""fieldType = ${tgtClass}Schema.${tgtProp}Type,""")
          } else {
            file.add(s"""fieldType = OptionType(${tgtClass}Schema.${tgtProp}Type),""")
          }

          val fetcherRef = s"${tgtClass}Schema.${tgtProp}By${ExportHelper.toClassName(h.target)}Fetcher"
          if (field.notNull) {
            file.add(s"resolve = ctx => $fetcherRef.defer(ctx.value.$srcProp)")
          } else {
            file.add(s"resolve = ctx => $fetcherRef.deferOpt(ctx.value.$srcProp)")
          }
          val comma = if (model.foreignKeys.lastOption.contains(fk)) { "" } else { "," }
          file.add(")" + comma, -1)
        case _ => // noop
      }
    }
  }
}
