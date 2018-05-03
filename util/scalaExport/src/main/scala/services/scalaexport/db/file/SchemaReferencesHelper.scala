package services.scalaexport.db.file

import models.scalaexport.db.ExportModel
import models.scalaexport.db.config.ExportConfiguration
import models.scalaexport.file.ScalaFile

object SchemaReferencesHelper {
  def writeFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val hasFk = model.foreignKeys.exists(_.references.size == 1)
    val references = model.transformedReferences(config)
    references.foreach { ref =>
      val srcModel = ref._3
      val srcField = ref._4
      val tgtField = ref._2
      if (srcModel.pkg != model.pkg) {
        file.addImport(srcModel.modelPackage.mkString("."), srcModel.className + "Schema")
      }
      file.add("Field(", 1)
      file.add(s"""name = "${ref._1.name}",""")
      file.add(s"""fieldType = ListType(${srcModel.className}Schema.${srcModel.propertyName}Type),""")

      val relationRef = s"${srcModel.className}Schema.${srcModel.propertyName}By${srcField.className}"
      val fetcherRef = relationRef + "Fetcher"
      val v = if (srcField.notNull) { s"c.value.${tgtField.propertyName}" } else { s"Some(c.value.${tgtField.propertyName})" }

      val call = if (ref._1.notNull) { "deferRelSeq" } else { "deferRelSeqOpt" }
      file.add(s"resolve = c => $fetcherRef.$call(", 1)
      file.add(s"${relationRef}Relation, $v")
      file.add(")", -1)

      val comma = if (model.pkColumns.isEmpty && references.lastOption.contains(ref) && !hasFk) { "" } else { "," }
      file.add(")" + comma, -1)
    }
  }
}
