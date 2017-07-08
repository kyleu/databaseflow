package models.scalaexport

case class ExportResult(
  id: String,
  models: Seq[String],
  files: Seq[ScalaFile]
)
