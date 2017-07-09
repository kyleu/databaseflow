package services.scalaexport

import models.schema.{Schema, Table}

object ExportTable {
  case class Reference(pkg: Seq[String], cls: String, prop: String, name: String, tgt: String, notNull: Boolean)
}

case class ExportTable(t: Table, config: ExportConfig.Result, s: Schema) {
  private[this] val asPropertyName = ExportHelper.toIdentifier(t.name)
  val propertyName = config.propertyNames.getOrElse(asPropertyName, asPropertyName)

  private[this] val asClassName = ExportHelper.toClassName(t.name)
  val className = config.classNames.getOrElse(asPropertyName, asClassName)

  val pkg = config.packages.get(propertyName).map(x => x.split("\\.").toList).getOrElse(Nil)

  val pkColumns = t.primaryKey.map(_.columns).getOrElse(Nil).map(c => t.columns.find(_.name == c).getOrElse {
    throw new IllegalStateException(s"Cannot derive primary key for [${t.name}] with key [${t.primaryKey}].")
  }).toList

  val referencingTables = s.tables.filter(tbl => tbl.name != t.name && tbl.foreignKeys.exists(_.targetTable == t.name))

  val references = referencingTables.flatMap { refTable =>
    refTable.foreignKeys.filter(_.targetTable == t.name).flatMap { fk =>
      fk.references.toList match {
        case Nil => None // noop
        case ref :: Nil =>
          val name = fk.name match {
            case x if t.columns.exists(_.name == x) => x + "FK"
            case x => x
          }
          val cls = ExportHelper.toClassName(refTable.name)
          val p = ExportHelper.toIdentifier(refTable.name)
          val pkg = config.packages.get(p).map(x => x.split("\\.").toList).getOrElse(Nil)
          val prop = ExportHelper.toIdentifier(ref.source)
          val tgt = ExportHelper.toIdentifier(ref.target)
          val tgtCol = t.columns.find(_.name == ref.target).getOrElse(throw new IllegalStateException(s"Missing column [${ref.target}]."))
          Some(ExportTable.Reference(pkg, cls, prop, name, tgt, tgtCol.notNull))
        case _ => None // multiple refs
      }
    }
  }
}
