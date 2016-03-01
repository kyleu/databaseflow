package utils

object NullUtils {
  // scalastyle:off
  val inst = null
  // scalastyle:on

  def isNull(v: Any) = v == inst
}
