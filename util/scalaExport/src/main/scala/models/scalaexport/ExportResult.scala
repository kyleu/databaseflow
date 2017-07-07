package models.scalaexport

case class ExportResult(
  id: String,
  files: Seq[(Seq[String], String, String)]
)
