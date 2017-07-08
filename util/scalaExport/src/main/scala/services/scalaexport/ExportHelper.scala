package services.scalaexport

import com.google.common.base.CaseFormat

object ExportHelper {
  val toScalaIdentifier = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL)
  val toScalaClassName = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.UPPER_CAMEL)

  val getAllArgs = "orderBy: Option[String] = None, limit: Option[Int] = None, offset: Option[Int] = None"
  val searchArgs = "q: String, orderBy: Option[String] = None, limit: Option[Int] = None, offset: Option[Int] = None"
}
