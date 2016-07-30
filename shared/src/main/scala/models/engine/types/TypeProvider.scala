package models.engine.types

trait TypeProvider {
  def columnTypes: Seq[String]
}
