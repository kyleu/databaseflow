package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportConfiguration, ExportModel}

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
      file.add(s"""name = "${ref._1.propertyName}",""")
      file.add(s"""fieldType = ListType(${srcModel.className}Schema.${srcModel.propertyName}Type),""")

      val relationRef = s"${srcModel.className}Schema.${srcModel.propertyName}By${srcField.className}"
      val fetcherRef = relationRef + "Fetcher"
      val v = if (srcField.notNull) { s"c.value.${tgtField.propertyName}" } else { s"Some(c.value.${tgtField.propertyName})" }
      if (SchemaHelper.shittyFetchers) {
        val call = if (ref._1.notNull) { "deferSeq" } else { "deferSeqOpt" }
        file.add(s"resolve = c => $fetcherRef.$call(Seq($v))")
      } else {
        val call = if (ref._1.notNull) { "deferRelSeq" } else { "deferRelSeqOpt" }
        file.add(s"resolve = c => $fetcherRef.$call(", 1)
        file.add(s"${relationRef}Relation, $v")
        file.add(")", -1)
      }
      val comma = if (references.lastOption.contains(ref) && !hasFk) { "" } else { "," }
      file.add(")" + comma, -1)
    }
  }
}
