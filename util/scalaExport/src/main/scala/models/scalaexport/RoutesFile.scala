package models.scalaexport

case class RoutesFile(override val key: String) extends OutputFile("conf", Nil, key, key + ".routes") {
  override def prefix = "# Generated File\n"
}
