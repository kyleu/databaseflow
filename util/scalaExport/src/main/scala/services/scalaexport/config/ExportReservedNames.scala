package services.scalaexport.config

object ExportReservedNames {
  private[this] val columnPropertyIds = Map(
    "name" -> "nameArg"
  )

  def getColumnPropertyId(col: String) = columnPropertyIds.getOrElse(col, col)
}
