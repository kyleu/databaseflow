package services.scalaexport

import com.google.common.base.{CaseFormat, Converter}

object ExportHelper {
  private[this] val converters = collection.mutable.HashMap.empty[(CaseFormat, CaseFormat), Converter[String, String]]

  private[this] def getSource(s: String) = s match {
    case _ if s.contains('-') => CaseFormat.LOWER_HYPHEN
    case _ if s.contains('_') => if (s.headOption.exists(_.isUpper)) { CaseFormat.UPPER_UNDERSCORE } else { CaseFormat.LOWER_UNDERSCORE }
    case _ => if (s.headOption.exists(_.isUpper)) { CaseFormat.UPPER_CAMEL } else { CaseFormat.LOWER_CAMEL }
  }

  private[this] def converterFor(src: CaseFormat, tgt: CaseFormat) = converters.getOrElseUpdate(src -> tgt, src.converterTo(tgt))

  def toIdentifier(s: String) = converterFor(getSource(s), CaseFormat.LOWER_CAMEL).convert(s.replaceAllLiterally(" ", "").replaceAllLiterally(".", ""))
  def toClassName(s: String) = converterFor(getSource(s), CaseFormat.UPPER_CAMEL).convert(s.replaceAllLiterally(" ", ""))
  def toDefaultTitle(s: String) = toClassName(s).flatMap(c => if (c.isUpper) { Seq(' ', c) } else { Seq(c) }).trim

  val getAllArgs = "orderBy: Option[String] = None, limit: Option[Int] = None, offset: Option[Int] = None"
  val searchArgs = "q: String, orderBy: Option[String] = None, limit: Option[Int] = None, offset: Option[Int] = None"
}
