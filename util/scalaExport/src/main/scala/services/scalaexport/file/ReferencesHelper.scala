package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportConfiguration

object ReferencesHelper {
  def writeFields(config: ExportConfiguration, model: ExportConfiguration.Model, file: ScalaFile) = if (model.references.nonEmpty) {
    model.references.foreach { ref =>
      val model = config.getModel(ref.srcTable)
      val prop = model.getField(ref.srcCol)
      if (model.pkg != model.pkg) {
        file.addImport(("models" +: model.pkg).mkString("."), model.className + "Schema")
      }
      file.add("Field(", 1)
      file.add(s"""name = "${ExportHelper.toIdentifier(ref.name)}",""")
      file.add(s"""fieldType = ListType(${model.className}Schema.${model.propertyName}Type),""")
      //file.add(s"""arguments = CommonSchema.limitArg :: CommonSchema.offsetArg :: Nil,""")

      val relationRef = s"${model.className}Schema.${model.propertyName}By${prop.className}"
      val fetcherRef = relationRef + "Fetcher"
      if (ref.notNull) {
        file.add(s"resolve = ctx => $fetcherRef.deferRelSeq($relationRef, ctx.value.${ref.tgt})")
      } else {
        file.add(s"resolve = ctx => $fetcherRef.deferRelSeqOpt($relationRef, ctx.value.${ref.tgt})")
      }
      val comma = if (model.references.lastOption.contains(ref) && model.foreignKeys.isEmpty) { "" } else { "," }
      file.add(")" + comma, -1)
    }
  }
}
