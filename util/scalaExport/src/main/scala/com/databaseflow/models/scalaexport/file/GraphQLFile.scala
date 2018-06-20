package com.databaseflow.models.scalaexport.file

case class GraphQLFile(
    override val pkg: Seq[String], override val key: String, override val core: Boolean = false
) extends OutputFile(dir = "data/graphql/explore", pkg = pkg, key = key, filename = key + ".graphql", core) {
  override def prefix = "# Generated File\n"
}
