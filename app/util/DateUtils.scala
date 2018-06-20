package util

import java.text.{DateFormat, SimpleDateFormat}

import org.joda.time._

object DateUtils {
  private[this] val fmt = org.joda.time.format.ISODateTimeFormat.dateTime()

  implicit def localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)
  implicit def localDateTimeOrdering: Ordering[LocalDateTime] = Ordering.fromLessThan(_ isBefore _)

  def today = DateTime.now(DateTimeZone.UTC).toLocalDate

  def now = DateTime.now(DateTimeZone.UTC).toLocalDateTime
  def nowMillis = toMillis(now)

  def toMillis(ldt: LocalDateTime) = ldt.toDateTime(DateTimeZone.UTC).getMillis
  def fromMillis(millis: Long) = new LocalDateTime(millis, DateTimeZone.UTC)

  def toIsoString(ldt: LocalDateTime) = fmt.print(ldt)
  def fromIsoString(s: String) = fmt.parseLocalDateTime(s)
  def fromDateString(s: String) = fmt.parseLocalDate(s)
  def fromTimeString(s: String) = fmt.parseLocalTime(s)

  def niceDate(d: LocalDate) = d.toString("EEEE, MMM dd, yyyy")
  def niceTime(d: LocalTime) = d.toString("HH:mm:ss")
  def niceDateTime(dt: LocalDateTime) = s"${niceDate(dt.toLocalDate)} ${niceTime(dt.toLocalTime)} UTC"

  private[this] val dFmt = new SimpleDateFormat("yyyy-MM-dd")
  def sqlDateFromString(s: String) = new java.sql.Date(dFmt.parse(s).getTime)

  private[this] val tFmt = new SimpleDateFormat("hh:mm:ss")
  def sqlTimeFromString(s: String) = new java.sql.Time(tFmt.parse(s).getTime)

  private[this] val dtFmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
  def sqlDateTimeFromString(s: String) = new java.sql.Timestamp(dtFmt.parse(s).getTime)

  private[this] val zonedDtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZ")
  def sqlZonedDateTimeFromString(s: String) = new java.sql.Timestamp(dtFmt.parse(s).getTime)
}
