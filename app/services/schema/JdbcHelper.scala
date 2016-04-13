package services.schema

object JdbcHelper {
  def intVal(a: Any) = a match {
    case i: Int => i
    case s: Short => s.toInt
    case bd: java.math.BigDecimal => bd.intValue
    case s: String => s.toInt
    case x => throw new IllegalArgumentException(s"Cannot parse [${x.getClass.getName}] as an int.")
  }

  def longVal(a: Any) = a match {
    case l: Long => l
    case f: Float => f.toLong
    case i: Int => i.toLong
    case bd: java.math.BigDecimal => bd.longValue
    case x => throw new IllegalArgumentException(x.getClass.getName)
  }

  def boolVal(a: Any) = a match {
    case b: Boolean => b
    case i: Int => i > 0
    case s: Short => s > 0
    case bd: java.math.BigDecimal => bd.intValue > 0
    case x => throw new IllegalArgumentException(s"Cannot parse [${x.getClass.getName}] as a boolean.")
  }
}
