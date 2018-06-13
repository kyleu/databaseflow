package com.databaseflow.models.scalaexport.file

case class RoutesFile(override val key: String) extends OutputFile(dir = "conf", pkg = Nil, key = key, filename = key + ".routes") {
  override def prefix = "# Generated File\n"
}
