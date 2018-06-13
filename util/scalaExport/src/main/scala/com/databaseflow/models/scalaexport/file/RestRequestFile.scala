package com.databaseflow.models.scalaexport.file

case class RestRequestFile(override val pkg: Seq[String], override val key: String) extends OutputFile(
  dir = "data/rest/request/explore", pkg = pkg, key = key, filename = key + ".json"
)
