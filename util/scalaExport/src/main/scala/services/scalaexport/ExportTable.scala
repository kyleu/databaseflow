package services.scalaexport

import models.schema.{Schema, Table}

case class ExportTable(t: Table, config: ExportConfig.Result, s: Schema) {
  val asClassName = ExportHelper.toScalaClassName.convert(t.name)
  val className = config.classNames.getOrElse(asClassName, asClassName)

  val asPropertyName = ExportHelper.toScalaIdentifier.convert(t.name)
  val propertyName = config.propertyNames.getOrElse(asPropertyName, asPropertyName)

  val pkg = config.packages.get(t.name).map(x => x.split("\\.").toList).getOrElse(Nil)

  val pkColumns = t.primaryKey.map(_.columns).getOrElse(Nil).map(c => t.columns.find(_.name == c).getOrElse {
    throw new IllegalStateException(s"Cannot derive primary key for [${t.name}] with key [${t.primaryKey}].")
  }).toList

  val referencingTables = s.tables.filter(t => t.foreignKeys.exists(_.targetTable == t.name))
}
