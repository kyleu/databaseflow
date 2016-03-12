package services.schema

import java.sql.DatabaseMetaData

import models.database.Row
import models.schema.{ PrimaryKey, Table }

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
}
