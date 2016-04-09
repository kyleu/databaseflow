package models.query

object RowDataOptions {
  val empty = RowDataOptions()
}

case class RowDataOptions(
  orderBy: Option[String] = None,
  filterCol: Option[String] = None,
  filterOp: Option[String] = None,
  filterVal: Option[String] = None,
  limit: Int = 100,
  offset: Int = 0
)
