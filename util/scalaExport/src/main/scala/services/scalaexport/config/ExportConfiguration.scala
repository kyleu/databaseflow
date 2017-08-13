package services.scalaexport.config

import models.schema.ColumnType

object ExportConfiguration {
  object Model {
    case class Field(
      columnName: String,
      propertyName: String,
      title: String,
      t: ColumnType,
      inSearch: Boolean = false,
      inView: Boolean = true,
      ignored: Boolean = false
    )
  }

  case class Model(
      tableName: String,
      pkg: Seq[String] = Nil,
      propertyName: String,
      className: String,
      title: String,
      plural: String,
      fields: Seq[Model.Field],
      extendsClass: Option[String] = None,
      ignored: Boolean = false,
      provided: Boolean = false
  ) {
    def getField(k: String) = getFieldOpt(k).getOrElse(throw new IllegalStateException(s"No field for model [$className] with name [$k]."))
    def getFieldOpt(k: String) = fields.find(f => f.columnName == k || f.propertyName == k)
  }
}

case class ExportConfiguration(
    key: String,
    projectId: String,
    projectTitle: String,
    models: Seq[ExportConfiguration.Model],
    engine: String = "postgres",
    projectLocation: Option[String] = None
) {
  def getModel(k: String) = getModelOpt(k).getOrElse(throw new IllegalStateException(s"No model available with name [$k]."))
  def getModelOpt(k: String) = models.find(m => m.tableName == k || m.propertyName == k || m.className == k)
}
