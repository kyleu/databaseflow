package com.databaseflow.models.scalaexport.db.config

import com.databaseflow.models.scalaexport.db.{ExportEnum, ExportField}
import models.schema._
import com.databaseflow.services.scalaexport.ExportHelper
import com.databaseflow.services.scalaexport.ExportHelper.{toDefaultTitle, toIdentifier}

object ExportConfigurationDefault {
  def forSchema(schema: Schema, loc: Option[String]) = {
    val key = ExportHelper.toIdentifier(schema.id)
    val enums = schema.enums.map { e =>
      val (name, pkg) = e.key match {
        case "setting_key" => "SettingKey" -> List("settings")
        case _ => ExportHelper.toClassName(ExportHelper.toIdentifier(e.key)) -> Nil
      }
      ExportEnum(pkg, e.key, name, Nil, e.values)
    }
    ExportConfiguration(
      key = key,
      projectId = key,
      projectTitle = ExportHelper.toClassName(key),
      flags = ExportFlag.values,
      enums = enums,
      models = schema.tables.map(t => ExportConfigurationDefaultTable.loadTableModel(schema, t, enums)),
      views = schema.views.map(v => ExportConfigurationDefaultView.loadViewModel(schema, v, enums)),
      projectLocation = loc
    )
  }

  private[this] def clean(str: String) = str match {
    case "type" => "typ"
    case _ => str
  }

  def loadField(col: Column, idx: Int, indexed: Boolean, unique: Boolean, inSearch: Boolean = false, enums: Seq[ExportEnum]) = ExportField(
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
    notNull = col.notNull,
    indexed = indexed,
    unique = unique,
    inSearch = inSearch,
    inSummary = inSearch
  )
}
