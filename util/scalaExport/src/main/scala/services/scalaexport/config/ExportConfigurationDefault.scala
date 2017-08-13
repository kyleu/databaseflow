package services.scalaexport.config

import models.schema.{Schema, Table}
import ExportConfiguration._
import services.scalaexport.ExportHelper
import services.scalaexport.ExportHelper._

object ExportConfigurationDefault {
  def forSchema(key: String, schema: Schema) = ExportConfiguration(
    key = key, projectId = key, projectTitle = ExportHelper.toClassName(key), models = schema.tables.map(loadModel)
  )

  private[this] def loadModel(table: Table) = {
    val cn = toClassName(table.name)
    Model(
      tableName = table.name,
      propertyName = toIdentifier(table.name),
      className = cn,
      title = cn,
      plural = cn + "s",
      fields = loadFields(table)
    )
  }

  private[this] def loadFields(t: Table) = t.columns.map { col =>
    val inSearch = t.primaryKey.exists(_.name == col.name) || t.indexes.exists(i => i.columns.exists(_.name == col.name))
    Model.Field(columnName = col.name, propertyName = toIdentifier(col.name), title = toClassName(col.name), t = col.columnType, inSearch = inSearch)
  }
}
