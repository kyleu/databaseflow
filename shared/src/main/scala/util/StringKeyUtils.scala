package util

object StringKeyUtils {
  private[this] def badChars = Seq(" " -> "_", "." -> "_", "(" -> "", ")" -> "", "#" -> "", "!" -> "")

  def cleanName(s: String) = {
    val swapped = badChars.foldLeft(s)((l, r) => l.replaceAllLiterally(r._1, r._2))
    if (swapped.head.isLetter) { swapped } else { "_" + swapped }
  }
}
