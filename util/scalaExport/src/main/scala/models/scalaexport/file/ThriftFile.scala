package models.scalaexport.file

case class ThriftFile(override val pkg: Seq[String], override val key: String) extends OutputFile(
  "doc/src/main/thrift", pkg, key, key + ".thrift"
) {
  override def prefix = "// Generated File\n"
}
