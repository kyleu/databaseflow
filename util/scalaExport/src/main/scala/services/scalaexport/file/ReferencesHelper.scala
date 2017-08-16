package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.config.{ExportConfiguration, ExportModel}

object ReferencesHelper {
  def writeFields(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val hasFk = model.foreignKeys.exists(_.references.size == 1)
    model.references.foreach { ref =>
      val srcModel = config.getModel(ref.srcTable)
      val srcField = srcModel.getField(ref.srcCol)
      val tgtField = model.getField(ref.tgt)
      if (srcModel.pkg != model.pkg) {
        file.addImport(srcModel.modelPackage.mkString("."), srcModel.className + "Schema")
      }
      file.add("Field(", 1)
      file.add(s"""name = "${ref.propertyName}",""")
      file.add(s"""fieldType = ListType(${srcModel.className}Schema.${srcModel.propertyName}Type),""")

      val relationRef = s"${srcModel.className}Schema.${srcModel.propertyName}By${srcField.className}"
      val fetcherRef = relationRef + "Fetcher"
      if (ref.notNull) {
        if (srcField.notNull) {
          file.add(s"resolve = ctx => $fetcherRef.deferSeq(Seq(ctx.value.${tgtField.propertyName}))")
        } else {
          file.add(s"resolve = ctx => $fetcherRef.deferSeq(Seq(Some(ctx.value.${tgtField.propertyName})))")
        }
      } else {
        if (srcField.notNull) {
          file.add(s"resolve = ctx => $fetcherRef.deferSeqOpt(Seq(ctx.value.${tgtField.propertyName}))")
        } else {
          file.add(s"resolve = ctx => $fetcherRef.deferSeqOpt(Seq(Some(ctx.value.${tgtField.propertyName})))")
        }
      }
      val comma = if (model.references.lastOption.contains(ref) && !hasFk) { "" } else { "," }
      file.add(")" + comma, -1)
    }
  }
}
