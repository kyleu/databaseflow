package services.scalaexport.config

case class ExportEnum(
  pkg: List[String] = Nil,
  name: String,
  className: String,
  values: Seq[String],
  ignored: Boolean = false
)
