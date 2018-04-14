package controllers.admin

import models.schema._
import models.scalaexport.db.{ExportEnum, ExportField, ExportModel}
import models.scalaexport.db.config.{ExportConfiguration, ExportConfigurationDefault, ExportConfigurationHelper}
import services.scalaexport.ExportHelper

import scala.util.control.NonFatal

object ScalaExportHelper {
  def merge(schema: Schema, config: ExportConfiguration) = {
    val enums = {
      val schEnums = schema.enums.map(e => ExportEnum(Nil, e.key, ExportHelper.toClassName(ExportHelper.toIdentifier(e.key)), e.values))
      (config.enums ++ schEnums).groupBy(_.className).map(_._2.head).toSeq
    }
    val models = schema.tables.map { t =>
      config.getModelOpt(t.name) match {
        case Some(m) =>
          val fields = t.columns.zipWithIndex.map { c =>
            m.fields.find(_.columnName == c._1.name).getOrElse(ExportConfigurationDefault.loadField(c._1, c._2, enums = config.enums))
          }
          m.copy(fields = fields.toList)
        case None => ExportConfigurationDefault.loadModel(schema, t, enums)
      }
    }
    config.copy(models = models)
  }

  def enumFor(e: EnumType, form: Map[String, String]) = try {
    ExportEnum(
      pkg = form("pkg").split('.').filter(_.nonEmpty).toList,
      name = e.key,
      className = form("className"),
      values = e.values,
      ignored = form.get("ignored").contains("true")
    )
  } catch {
    case NonFatal(x) => throw new IllegalStateException(s"Unable to create model for enum [${e.key}].", x)
  }

  def modelForTable(schema: Schema, t: Table, form: Map[String, String], enums: Seq[ExportEnum]) = try {
    ExportModel(
      tableName = t.name,
      pkg = form("pkg").split('.').filter(_.nonEmpty).toList,
      propertyName = form("propertyName"),
      className = form("className"),
      title = form("title"),
      description = form.get("description").filter(_.nonEmpty),
      plural = form("plural"),
      fields = t.columns.zipWithIndex.map { col =>
        fieldForColumn(col._1, col._2, form.filter(_._1.startsWith("field.")).map(x => x._1.stripPrefix("field.") -> x._2), enums)
      }.toList,
      pkColumns = ExportConfigurationHelper.pkColumns(schema, t),
      foreignKeys = t.foreignKeys.groupBy(x => x.references).map(_._2.head).toList,
      references = ExportConfigurationHelper.references(schema, t),
      extendsClass = form.get("extendsClass").filter(_.nonEmpty),
      icon = form.get("icon").filter(_.nonEmpty),
      scalaJs = form.get("scalaJs").contains("true"),
      ignored = form.get("ignored").contains("true"),
      audited = form.get("audited").contains("true"),
      provided = form.get("provided").contains("true")
    )
  } catch {
    case NonFatal(x) => throw new IllegalStateException(s"Unable to create model for table [${t.name}].", x)
  }

  def fieldForColumn(col: Column, idx: Int, form: Map[String, String], enums: Seq[ExportEnum]) = {
    ExportField(
      columnName = col.name,
      propertyName = form(col.name + ".propertyName"),
      title = form(col.name + ".title"),
      description = form.get(col.name + ".description").filter(_.nonEmpty),
      idx = idx,
      t = enums.find(_.name == col.sqlTypeName).map(_ => ColumnType.EnumType).getOrElse(ColumnType.withValue(form(col.name + ".t"))),
      sqlTypeName = col.sqlTypeName,
      enumOpt = enums.find(_.name == col.sqlTypeName),
      defaultValue = form.get(col.name + ".defaultValue").orElse(col.defaultValue),
      notNull = form.get(col.name + ".notNull").map(_ == "true").getOrElse(col.notNull),
      inSearch = form.get(col.name + ".inSearch").contains("true"),
      inView = form.get(col.name + ".inView").contains("true"),
      inSummary = form.get(col.name + ".inSummary").contains("true"),
      ignored = form.get(col.name + ".ignored").contains("true")
    )
  }
}
