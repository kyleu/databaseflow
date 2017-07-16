package models.query

object RowDataOptions {
  val empty = RowDataOptions()
}

case class RowDataOptions(
    orderByCol: Option[String] = None,
    orderByAsc: Option[Boolean] = None,
    filters: Seq[QueryFilter] = Nil,
    whereClause: Option[String] = None,
    limit: Option[Int] = None,
    offset: Option[Int] = None
) {
  def merge(rdo: RowDataOptions) = {
    val (oc, oa) = rdo.orderByCol match {
      case Some(_) => rdo.orderByCol -> rdo.orderByAsc
      case None => orderByCol -> orderByAsc
    }

    RowDataOptions(
      orderByCol = oc,
      orderByAsc = oa,
      filters = rdo.filters ++ filters,
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
    filters = filters,
    dataOffset = offset.getOrElse(0)
  )

  def isFiltered = filters.nonEmpty

  override def toString = Seq(
    orderByCol.map("Order By: " + _),
    orderByAsc.map("Asc: " + _),
    if(filters.isEmpty) {
      None
    } else {
      Some(filters.map("Filter: " + _).mkString(", "))
    },
    limit.map("Limit: " + _),
    offset.map("Offset: " + _)
  ).flatten.mkString(", ")
}
