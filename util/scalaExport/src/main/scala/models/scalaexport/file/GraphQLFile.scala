package models.scalaexport.file

case class GraphQLFile(override val pkg: Seq[String], override val key: String) extends OutputFile(
  dir = "data/graphql/explore", pkg = pkg, key = key, filename = key + ".graphql"
) {
  override def prefix = "# Generated File\n"
}
