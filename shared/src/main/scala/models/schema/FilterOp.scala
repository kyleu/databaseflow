package models.schema

import enumeratum._

sealed abstract class FilterOp(val key: String, val symbol: String, val title: String, val sqlSymbol: String) extends EnumEntry {
  override def toString = key
}

object FilterOp extends Enum[FilterOp] with CirceEnum[FilterOp] {
  case object Equal extends FilterOp("eq", "=", "Equal", "=")
  case object NotEqual extends FilterOp("neq", "≠", "Not Equal", "!=")
  case object LessThan extends FilterOp("lt", "<", "Less Than", "<")
  case object GreaterThan extends FilterOp("gt", ">", "Greater Than", ">")
  case object LessThanOrEqual extends FilterOp("lte", "≤", "Less Than Or Equal", "<=")
  case object GreaterThanOrEqual extends FilterOp("gte", "≥", "Greater Than Or Equal", ">=")
  case object In extends FilterOp("in", "in", "In", "in")
  case object Like extends FilterOp("like", "like", "Like", "like")
  case object Between extends FilterOp("btw", "between", "Between", "between")
  case object IsNull extends FilterOp("nl", "is null", "Is Null", "is null")
  case object IsNotNull extends FilterOp("nnl", "is not null", "Is Not Null", "is not null")

  override val values = findValues
}
