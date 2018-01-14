package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportConfiguration, ExportModel}

object SchemaForeignKey {
  def writeSchema(config: ExportConfiguration, src: ExportModel, file: ScalaFile) = if (src.foreignKeys.nonEmpty) {
    file.addImport("sangria.execution.deferred", "Fetcher")
    val fks = src.foreignKeys.filter(_.references.lengthCompare(1) == 0)
    fks.foreach { fk =>
      fk.references.toList match {
        case Nil => // noop
        case h :: Nil => config.getModelOpt(fk.targetTable).foreach(_ => {
          val srcCol = src.getField(h.source)
          if (src.pkColumns.isEmpty) {
            val idType = if (srcCol.notNull) { srcCol.scalaType } else { "Option[" + srcCol.scalaType + "]" }
            srcCol.addImport(file)
            file.addImport("sangria.execution.deferred", "HasId")
            val fn = s"${src.propertyName}By${srcCol.className}Fetcher"
            file.addMarker("fetcher", (src.modelPackage :+ s"${src.className}Schema" :+ fn).mkString("."))
            file.add(s"val $fn = Fetcher { (c: GraphQLContext, values: Seq[$idType]) =>", 1)
            file.add(s"c.${src.serviceReference}.getBy${srcCol.className}Seq(c.creds, values)(c.trace)")
            file.add(s"}(HasId[${src.className}, $idType](_.${srcCol.propertyName}))", -1)
            file.add()
          } else {
            val relName = s"${src.propertyName}By${srcCol.className}"
            val idType = if (srcCol.notNull) { srcCol.scalaType } else { "Option[" + srcCol.scalaType + "]" }

            file.addMarker("fetcher", (src.modelPackage :+ s"${src.className}Schema" :+ s"${relName}Fetcher").mkString("."))
            file.addImport("sangria.execution.deferred", "Relation")
            srcCol.addImport(file)
            file.add(s"""val ${relName}Relation = Relation[${src.className}, $idType]("by${srcCol.className}", x => Seq(x.${srcCol.propertyName}))""")
            file.add(s"val ${relName}Fetcher = Fetcher.rel[GraphQLContext, ${src.className}, ${src.className}, ${src.pkType}](", 1)
            val rels = if (srcCol.notNull) { s"rels(${relName}Relation)" } else { s"rels(${relName}Relation).flatten" }
            file.add(s"getByPrimaryKeySeq, (c, rels) => c.${src.serviceReference}.getBy${srcCol.className}Seq(c.creds, $rels)(c.trace)")
            file.add(")", -1)

            file.add()
          }
        })
        case _ => // noop
      }
    }
  }

  def writeFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = if (model.foreignKeys.nonEmpty) {
    val fks = model.foreignKeys.filter(_.references.lengthCompare(1) == 0)
    fks.foreach { fk =>
      config.getModelOpt(fk.targetTable).foreach { targetTable =>
        val fkCols = fk.references
        val fields = fkCols.map(h => model.fields.find(_.columnName == h.source).getOrElse {
          throw new IllegalStateException(s"Missing source column [${h.source}].")
        })
        val targets = fkCols.map(h => targetTable.fields.find(_.columnName == h.target).getOrElse {
          throw new IllegalStateException(s"Missing target column [${h.target}].")
        })
        fields.foreach(f => f.addImport(file))
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
            val comma = if (model.pkColumns.isEmpty && fks.lastOption.contains(fk)) { "" } else { "," }
            file.add(")" + comma, -1)
          case _ => throw new IllegalStateException(s"Unhandled foreign key references [${fields.map(_.propertyName).mkString(", ")}].")
        }
      }
    }
  }
}
