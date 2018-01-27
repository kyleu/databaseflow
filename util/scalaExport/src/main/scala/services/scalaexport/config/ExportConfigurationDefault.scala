package services.scalaexport.config

import models.schema.{Column, ColumnType, Schema, Table}
import services.scalaexport.ExportHelper
import services.scalaexport.ExportHelper._

object ExportConfigurationDefault {
  def forSchema(key: String, schema: Schema) = {
    val enums = schema.enums.map(e => ExportEnum(Nil, e.key, ExportHelper.toClassName(ExportHelper.toIdentifier(e.key)), e.values))
    ExportConfiguration(
      key = key,
      projectId = key,
      projectTitle = ExportHelper.toClassName(key),
      enums = enums,
      models = schema.tables.map(t => loadModel(schema, t, enums))
    )
  }

  def loadModel(schema: Schema, t: Table, enums: Seq[ExportEnum]) = {
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
      fields = loadFields(t, enums),
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

  private[this] def loadFields(t: Table, enums: Seq[ExportEnum]) = t.columns.zipWithIndex.toList.map { col =>
    val inSearch = t.primaryKey.exists(_.name == col._1.name) || t.indexes.exists(i => i.columns.exists(_.name == col._1.name))
    loadField(col._1, col._2, inSearch, enums)
  }

  def loadField(col: Column, idx: Int, inSearch: Boolean = false, enums: Seq[ExportEnum]) = ExportField(
    columnName = col.name,
    propertyName = clean(toIdentifier(col.name)),
    title = toDefaultTitle(col.name),
    description = col.description,
    idx = idx,
    t = col.columnType,
    sqlTypeName = col.sqlTypeName,
    enumOpt = col.columnType match {
      case ColumnType.EnumType => Some(enums.find(_.name == col.sqlTypeName).getOrElse {
        throw new IllegalStateException(s"Cannot find enum [${col.sqlTypeName}] among [${enums.size}] enums.")
      })
      case _ => None
    },
    defaultValue = col.defaultValue,
    inSearch = inSearch,
    inSummary = inSearch
  )
}
