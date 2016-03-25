package utils

import org.joda.time.{ LocalDateTime, LocalTime, LocalDate }

object Formatter {
  def niceDate(d: LocalDate) = d.toString("EEEE, MMM dd, yyyy")
  def niceTime(d: LocalTime) = d.toString("HH:mm:ss")
  def niceDateTime(dt: LocalDateTime) = s"${niceDate(dt.toLocalDate)} ${niceTime(dt.toLocalTime)} UTC"

  def className(instance: Any) = instance.getClass.getSimpleName.stripSuffix("$")
}
