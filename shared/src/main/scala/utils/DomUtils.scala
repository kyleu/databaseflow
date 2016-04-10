package utils

object DomUtils {
  def cleanForId(s: String) = s.replaceAllLiterally("$", "")
}
