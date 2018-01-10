package services.scalaexport.config

import models.schema.{Column, Schema, Table}
import services.scalaexport.ExportHelper
import services.scalaexport.ExportHelper._

object ExportConfigurationDefault {
  def forSchema(key: String, schema: Schema) = ExportConfiguration(
    key = key,
    projectId = key,
    projectTitle = ExportHelper.toClassName(key),
    enums = schema.enums.map(e => ExportEnum(Nil, e.key, ExportHelper.toClassName(ExportHelper.toIdentifier(e.key)), e.values)),
    models = schema.tables.map(t => loadModel(schema, t))
  )

  def loadModel(schema: Schema, t: Table) = {
    val cn = t.name match {
      case "system_users" => "SystemUser"
      case x => toClassName(x)
    }

    val provided = cn match {
      case "Ddl" | "LoginInfo" | "PasswordInfo" | "SettingValues" => true
      case _ => false
    }

    val pkg = cn match {
      case "Ddl" => List("ddl")
      case "Audit" => List("audit")
      case "AuditRecord" => List("audit")
      case "SystemUser" => List("user")
      case "Note" => List("note")
      case "LoginInfo" | "PasswordInfo" => List("auth")
      case _ => Nil
    }

    ExportModel(
      tableName = t.name,
      pkg = pkg,
      propertyName = toIdentifier(cn),
      className = cn,
      title = toDefaultTitle(cn),
      description = t.description,
      plural = toDefaultTitle(cn) + "s",
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
    loadField(col, inSearch)
  }

  def loadField(col: Column, inSearch: Boolean = false) = ExportField(
    columnName = col.name,
    propertyName = clean(toIdentifier(col.name)),
    title = toDefaultTitle(col.name),
    description = col.description,
    t = col.columnType,
    sqlTypeName = col.sqlTypeName,
    defaultValue = col.defaultValue,
    inSearch = inSearch,
    inSummary = inSearch
  )
}
