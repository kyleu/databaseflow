package models.query

object RowDataOptions {
  val empty = RowDataOptions()
}

case class RowDataOptions(
  orderByCol: Option[String] = None,
  orderByAsc: Option[Boolean] = None,
  filterCol: Option[String] = None,
  filterOp: Option[String] = None,
  filterVal: Option[String] = None,
  limit: Option[Int] = None,
  offset: Option[Int] = None
)
