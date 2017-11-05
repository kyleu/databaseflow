package models.result

case class QueryResultRow(columns: Seq[String], data: Seq[Option[String]]) {
  private[this] val columnIndexes = columns.zipWithIndex.toMap
  private[this] def colIndex(col: String) = columnIndexes.getOrElse(col, throw new IllegalStateException(s"Invalid column [$col]."))

  def getCell(col: String) = data(colIndex(col))
  def getRequiredCell(col: String) = getCell(col).getOrElse(throw new IllegalStateException(s"Null value for column [$col]."))
}
