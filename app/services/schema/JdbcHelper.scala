package services.schema

object JdbcHelper {
  def intVal(a: Any) = a match {
    case i: Int => i
    case s: Short => s.toInt
    case bd: java.math.BigDecimal => bd.intValue
    case s: String => s.toInt
    case x => throw new IllegalArgumentException(s"Cannot parse [${x.getClass.getName}] as an int.")
  }
}
