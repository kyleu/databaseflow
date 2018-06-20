package models.schema

case class ClientInfoProperty(
    name: String,
    description: Option[String],
    maxLength: Int,
    defaultValue: Option[String]
)
