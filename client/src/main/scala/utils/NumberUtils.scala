package utils

object NumberUtils {
  def withCommas(i: Int) = {
    val sections = i.toString.grouped(3)
    utils.Logging.info(sections.mkString(","))
  }
}
