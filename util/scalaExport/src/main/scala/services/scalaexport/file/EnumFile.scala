package services.scalaexport.file

import models.scalaexport.ScalaFile
import services.scalaexport.ExportHelper
import services.scalaexport.config.ExportEnum

object EnumFile {
  def export(model: ExportEnum) = {
    val file = ScalaFile(model.modelPackage, model.className, Some(ScalaFile.sharedSrc))

    file.addImport("enumeratum.values", "StringEnumEntry")
    file.addImport("enumeratum.values", "StringEnum")
    file.addImport("enumeratum.values", "StringCirceEnum")

    file.add(s"sealed abstract class ${model.className}(override val value: String) extends StringEnumEntry")
    file.add()

    file.add(s"object ${model.className} extends StringEnum[${model.className}] with StringCirceEnum[${model.className}] {", 1)
    addFields(model, file)
    file.add()
    file.add("override val values = findValues")
    file.add("}", -1)

    file
  }

  private[this] def addFields(model: ExportEnum, file: ScalaFile) = model.values.foreach { v =>
    val cn = ExportHelper.toClassName(ExportHelper.toIdentifier(v))
    file.add(s"""case object $cn extends ${model.className}("$v")""")
  }
}
