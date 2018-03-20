package models.scalaexport.file

case class RestQueryFile(override val pkg: Seq[String], override val key: String) extends OutputFile(
  dir = "data/rest/request/explore", pkg = pkg, key = key, filename = key + ".json"
) {
  override def prefix = "/* Generated File */\n"
}
