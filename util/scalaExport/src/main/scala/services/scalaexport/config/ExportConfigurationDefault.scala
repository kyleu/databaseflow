package services.scalaexport.config

import models.schema.{Schema, Table}
import services.scalaexport.ExportHelper
import services.scalaexport.ExportHelper._

object ExportConfigurationDefault {
  def forSchema(key: String, schema: Schema) = ExportConfiguration(
    key = key,
    projectId = key,
    projectTitle = ExportHelper.toClassName(key),
    models = schema.tables.map(t => loadModel(schema, t)),
    engine = ExportEngine.withNameInsensitive(schema.engine)
  )

  def loadModel(schema: Schema, t: Table) = {
    val cn = toClassName(t.name)

    val provided = cn match {
      case "Ddl" | "Users" | "LoginInfo" | "PasswordInfo" | "SettingValues" => true
      case _ => false
    }

    ExportModel(
      tableName = t.name,
      propertyName = toIdentifier(t.name),
      className = cn,
      title = cn,
      description = t.description,
      plural = cn + "s",
      fields = loadFields(t),
      pkColumns = ExportConfigurationHelper.pkColumns(schema, t),
      foreignKeys = t.foreignKeys.toList,
      references = ExportConfigurationHelper.references(schema, t),
      provided = provided
    )
  }

  private[this] def clean(str: String) = str match {
    case "type" => "typ"
    case _ => str
  }

  private[this] def loadFields(t: Table) = t.columns.toList.map { col =>
    val inSearch = t.primaryKey.exists(_.name == col.name) || t.indexes.exists(i => i.columns.exists(_.name == col.name))
    ExportField(
      columnName = col.name,
      propertyName = clean(toIdentifier(col.name)),
      title = toClassName(col.name),
      description = col.description,
      t = col.columnType,
      sqlTypeName = col.sqlTypeName,
      defaultValue = col.defaultValue,
      inSearch = inSearch
    )
  }
}
