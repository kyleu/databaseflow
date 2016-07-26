package utils.web

import play.api.data.FormError

object PlayFormUtils {
  private[this] val numFormatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)

  def withCommas(i: Int) = numFormatter.format(i.toLong)
  def withCommas(l: Long) = numFormatter.format(l)
  def withCommas(d: Double) = numFormatter.format(d)

  def errorsToString(errors: Seq[FormError]) = errors.map(e => e.key + ": " + e.message).mkString(", ")
}
