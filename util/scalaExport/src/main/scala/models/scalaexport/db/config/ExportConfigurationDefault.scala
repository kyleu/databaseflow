package models.scalaexport.db.config

import models.scalaexport.db.{ExportEnum, ExportField, ExportModel}
import models.schema.{Column, ColumnType, Schema, Table}
import services.scalaexport.ExportHelper
import services.scalaexport.ExportHelper.{toClassName, toDefaultTitle, toIdentifier}

object ExportConfigurationDefault {
  def forSchema(schema: Schema) = {
    val key = ExportHelper.toIdentifier(schema.id)
    val enums = schema.enums.map { e =>
      val pkg = e.key match {
        case "setting_key" => List("settings")
        case _ => Nil
      }
      ExportEnum(pkg, e.key, ExportHelper.toClassName(ExportHelper.toIdentifier(e.key)), e.values)
    }
    ExportConfiguration(
      key = key,
      projectId = key,
      projectTitle = ExportHelper.toClassName(key),
      flags = Set("rest", "graphql", "openapi"),
      enums = enums,
      models = schema.tables.map(t => loadModel(schema, t, enums))
    )
  }

  def loadModel(schema: Schema, t: Table, enums: Seq[ExportEnum]) = {
    val audited = t.name match {
      case "system_users" => true
      case x => false
    }

    val cn = t.name match {
      case "system_users" => "SystemUser"
      case "oauth2_info" => "OAuth2Info"
      case x => toClassName(x)
    }

    val provided = cn match {
      case "Ddl" | "LoginInfo" | "OAuth2Info" | "PasswordInfo" | "SettingValues" => true
      case _ => false
    }

    val pkg = cn match {
      case "Ddl" => List("ddl")
      case "Audit" => List("audit")
      case "AuditRecord" => List("audit")
      case "OAuth2Info" => List("auth")
      case "PasswordInfo" => List("auth")
      case "ScheduledTaskRun" => List("task")
      case "SyncProgress" => List("sync")
      case "SystemUser" => List("user")
      case "Note" => List("note")
      case "LoginInfo" | "PasswordInfo" => List("auth")
      case _ => Nil
    }

    val title = toDefaultTitle(cn) match {
      case "O Auth2 Info" => "OAuth2 Info"
      case x => x
    }

    val plural = title match {
      case "Sync Progress" => "Sync Progresses"
      case x => x + "s"
    }

    ExportModel(
      tableName = t.name,
      pkg = pkg,
      propertyName = toIdentifier(cn),
      className = cn,
      title = title,
      description = t.description,
      plural = plural,
      fields = loadFields(t, enums),
      pkColumns = ExportConfigurationHelper.pkColumns(schema, t),
      foreignKeys = t.foreignKeys.groupBy(x => x.references).map(_._2.head).toList,
      references = ExportConfigurationHelper.references(schema, t, Map.empty),
      audited = audited,
      provided = provided
    )
  }

  private[this] def clean(str: String) = str match {
    case "type" => "typ"
    case _ => str
  }

  private[this] def loadFields(t: Table, enums: Seq[ExportEnum]) = t.columns.zipWithIndex.toList.map { col =>
    val banned = t.name match {
      case "audit_record" if col._1.name == "changes" => true
      case _ => false
    }
    val inPk = t.primaryKey.exists(_.name == col._1.name)
    val inIndex = t.indexes.exists(i => i.columns.exists(_.name == col._1.name))
    def extras = t.name match {
      case "audit_record" => Set("changes")
      case "note" => Set("rel_type", "rel_pk", "text", "author", "created")
      case "sync_progress" => Set("message", "last_time")
      case "scheduled_task_run" => Set("arguments")
      case _ => Set.empty[String]
    }
    val inSearch = (!banned) && (inPk || inIndex || extras(col._1.name))
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
