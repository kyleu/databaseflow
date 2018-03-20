package models.scalaexport.file

case class ThriftFile(override val pkg: Seq[String], override val key: String) extends OutputFile(
  dir = "doc/src/main/thrift", pkg = pkg, key = key, filename = key + ".thrift"
) {
  override def prefix = "// Generated File\n"
}
