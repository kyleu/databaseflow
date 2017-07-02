package models.query

import models.schema.{ColumnType, FilterOp}

case class QueryFilter(col: String, op: FilterOp, t: ColumnType, v: String)
