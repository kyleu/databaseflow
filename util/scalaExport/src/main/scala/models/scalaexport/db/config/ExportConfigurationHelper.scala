package models.scalaexport.db.config

import models.scalaexport.db.ExportModel
import models.schema.{Schema, Table}

object ExportConfigurationHelper {
  def pkColumns(schema: Schema, t: Table) = {
    t.primaryKey.map(_.columns).getOrElse(Nil).map(c => t.columns.find(_.name == c).getOrElse {
      throw new IllegalStateException(s"Cannot derive primary key for [${t.name}] with key [${t.primaryKey}].")
    })
  }

  def references(schema: Schema, t: Table, form: Map[String, String]) = {
    val referencingTables = schema.tables.filter(tbl => tbl.name != t.name && tbl.foreignKeys.exists(_.targetTable == t.name))

    referencingTables.toList.flatMap { refTable =>
      refTable.foreignKeys.filter(_.targetTable == t.name).flatMap { fk =>
        fk.references match {
          case Nil => Nil // noop
          case ref :: Nil =>
            val name = fk.name match {
              case x if form.get(s"ref.$x.propertyName").exists(_.trim.nonEmpty) => form(s"ref.$x.propertyName").trim
              case x if t.columns.exists(_.name == x) => x + "FK"
              case x => x
            }
            val tgtCol = t.columns.find(_.name == ref.target).getOrElse(throw new IllegalStateException(s"Missing column [${ref.target}]."))
            Seq(ExportModel.Reference(name = name, srcTable = refTable.name, srcCol = ref.source, tgt = ref.target, notNull = tgtCol.notNull))
          case _ => None // multiple refs
        }
      }
    }
  }
}
