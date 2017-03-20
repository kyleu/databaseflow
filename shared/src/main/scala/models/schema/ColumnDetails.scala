package models.schema

case class ColumnDetails(
  count: Long,
  distinctCount: Long,
  min: Option[Long] = None,
  max: Option[Long] = None,
  sum: Option[Long] = None,
  avg: Option[Long] = None,
  error: Option[String] = None
)
