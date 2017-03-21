package models.schema

case class ColumnDetails(
  count: Long,
  distinctCount: Long,
  min: Option[Double] = None,
  max: Option[Double] = None,
  sum: Option[Double] = None,
  avg: Option[Double] = None,
  variance: Option[Double] = None,
  stdDev: Option[Double] = None,
  error: Option[String] = None
)
