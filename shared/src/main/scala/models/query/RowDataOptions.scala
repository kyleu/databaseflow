package models.query

import models.schema.FilterOp

object RowDataOptions {
  val empty = RowDataOptions()
}

case class RowDataOptions(
    orderByCol: Option[String] = None,
    orderByAsc: Option[Boolean] = None,
    filterCol: Option[String] = None,
    filterOp: Option[FilterOp] = None,
    filterVal: Option[String] = None,
    limit: Option[Int] = None,
    offset: Option[Int] = None
) {
  def toSource(t: String, name: String) = QueryResult.Source(
    t = t,
    name = name,
    sortable = true,
    sortedColumn = orderByCol,
    sortedAscending = orderByAsc,
    filterColumn = filterCol,
    filterOp = filterOp,
    filterValue = filterVal,
    dataOffset = offset.getOrElse(0)
  )

  def isFiltered = filterCol.isDefined

  override def toString = Seq(
    orderByCol.map("Order By: " + _),
    orderByAsc.map("Asc: " + _),
    filterCol.map("Filter Col: " + _),
    filterOp.map("Op: " + _),
    filterVal.map("Val: " + _),
    limit.map("Limit: " + _),
    offset.map("Offset: " + _)
  ).flatten.mkString(", ")
}
