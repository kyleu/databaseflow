package services.scalaexport

import com.google.common.base.CaseFormat

object ScalaExportHelper {
  val toScalaIdentifier = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL)
  val toScalaClassName = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL)
}
