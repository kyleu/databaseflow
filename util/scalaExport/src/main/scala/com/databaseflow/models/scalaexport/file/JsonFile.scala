package com.databaseflow.models.scalaexport.file

case class JsonFile(
    override val pkg: Seq[String], override val key: String, root: Option[String] = None
) extends OutputFile(dir = root.getOrElse("conf/openapi"), pkg = pkg, key = key, filename = key + ".json") {
  override def prefix = s"// Generated File\n"
}
