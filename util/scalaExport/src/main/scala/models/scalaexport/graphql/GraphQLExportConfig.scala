package models.scalaexport.graphql

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object GraphQLExportConfig {
  implicit val jsonEncoder: Encoder[GraphQLExportConfig] = deriveEncoder
  implicit val jsonDecoder: Decoder[GraphQLExportConfig] = deriveDecoder
}

case class GraphQLExportConfig(input: String, output: String, rootPrefix: String, pkg: String) {
  def pkgSeq = pkg.split('.').map(_.trim).filter(_.nonEmpty)
}
