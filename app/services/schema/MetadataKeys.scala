package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.schema.{ Reference, ForeignKey, PrimaryKey, Table }

object MetadataKeys {
  def getPrimaryKey(metadata: DatabaseMetaData, table: Table) = {
    val rs = metadata.getPrimaryKeys(table.catalog.orNull, table.schema.orNull, table.name)

    val keys = new Row.Iter(rs).map { row =>
      (row.as[String]("PK_NAME"), row.as[String]("COLUMN_NAME"), row.as[Int]("KEY_SEQ"))
    }.toList.groupBy(_._1)

    if(keys.size > 1) {
      throw new IllegalStateException("Multiple primary keys?")
    }

    keys.map { k =>
      PrimaryKey(
        name = k._1,
        columns = k._2.sortBy(_._3).map(_._2)
      )
    }.toList.headOption
  }

  def getForeignKeys(metadata: DatabaseMetaData, table: Table) = {
    val rs = metadata.getImportedKeys(table.catalog.orNull, table.schema.orNull, table.name)

    val rows = new Row.Iter(rs).map { row =>
      val name = row.as[String]("fk_name")
      val targetTable = row.as[String]("pktable_name")
      val targetColumn = row.as[String]("pkcolumn_name")
      val sourceColumn = row.as[String]("fkcolumn_name")
      val order = row.as[Int]("key_seq")
      val updateRule = ruleFor(row.as[Int]("update_rule"))
      val deleteRule = ruleFor(row.as[Int]("delete_rule"))
      (name, sourceColumn, targetTable, targetColumn, order, updateRule, deleteRule)
    }.toList.groupBy(_._1)

    rows.map { row =>
      val first = row._2.head
      ForeignKey(
        name = first._1,
        targetTable = first._3,
        references = row._2.map { col =>
          Reference(col._2, col._4)
        }
      )
    }.toList
  }

  private[this] def ruleFor(i: Int) = i match {
    case DatabaseMetaData.importedKeyNoAction | DatabaseMetaData.importedKeyRestrict => "none"
    case DatabaseMetaData.importedKeyCascade => "cascade"
    case DatabaseMetaData.importedKeySetNull => "null"
    case DatabaseMetaData.importedKeySetDefault => "default"
    case _ => throw new IllegalArgumentException(i.toString)
  }
}
