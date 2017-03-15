package models.query

import models.schema.{ColumnType, FilterOp}

object RowDataOptions {
  val empty = RowDataOptions()
}

case class RowDataOptions(
    orderByCol: Option[String] = None,
    orderByAsc: Option[Boolean] = None,
    filterCol: Option[String] = None,
    filterOp: Option[FilterOp] = None,
    filterType: Option[ColumnType] = None,
    filterVal: Option[String] = None,
    limit: Option[Int] = None,
    offset: Option[Int] = None
) {
  def merge(rdo: RowDataOptions) = {
    val (oc, oa) = rdo.orderByCol match {
      case Some(_) => rdo.orderByCol -> rdo.orderByAsc
      case None => orderByCol -> orderByAsc
    }

    val (fc, fo, ft, fv) = rdo.filterCol match {
      case Some(_) => (rdo.filterCol, rdo.filterOp, rdo.filterType, rdo.filterVal)
      case None => (filterCol, filterOp, filterType, filterVal)
    }

    RowDataOptions(
      orderByCol = oc,
      orderByAsc = oa,
      filterCol = fc,
      filterOp = fo,
      filterType = ft,
      filterVal = fv,
      limit = rdo.limit.orElse(limit),
      offset = rdo.offset.orElse(offset)
    )
  }

  def toSource(t: String, name: String, sortable: Boolean) = QueryResult.Source(
    t = t,
    name = name,
    sortable = sortable,
    sortedColumn = orderByCol,
    sortedAscending = orderByAsc,
    filterColumn = filterCol,
    filterOp = filterOp,
    filterType = filterType,
    filterValue = filterVal,
    dataOffset = offset.getOrElse(0)
  )

  def isFiltered = filterCol.isDefined

  override def toString = Seq(
    orderByCol.map("Order By: " + _),
    orderByAsc.map("Asc: " + _),
    filterCol.map("Filter Col: " + _),
    filterOp.map("Op: " + _),
    filterType.map("Type: " + _),
    filterVal.map("Val: " + _),
    limit.map("Limit: " + _),
    offset.map("Offset: " + _)
  ).flatten.mkString(", ")
}
