package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportConfiguration, ExportModel}

object ForeignKeyFields {
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
