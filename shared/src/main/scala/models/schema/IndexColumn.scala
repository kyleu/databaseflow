package models.schema

case class IndexColumn(name: String, ascending: Boolean) {
  override def toString = name + (if (ascending) { "" } else { " (desc)" })
}
