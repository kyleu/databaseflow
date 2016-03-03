package services.schema

import models.schema._
import org.apache.ddlutils.{ model => ddl }

object SchemaConverter {
  def convert(db: ddl.Database) = {
    Database(
      name = db.getName,
      version = db.getVersion,
      tables = db.getTables.map { t =>
        Table(
          name = t.getName,
          catalog = Option(t.getCatalog),
          schema = t.getSchema,
          description = Option(t.getDescription),
          typeName = t.getType,

          columns = t.getColumns.map { c =>
            Column(
              name = c.getName,
              description = Option(c.getDescription),
              primaryKey = c.isPrimaryKey,
              notNull = c.isRequired,
              autoIncrement = c.isAutoIncrement,
              typeCode = c.getTypeCode,
              typeName = c.getType,
              size = c.getSize,
              sizeAsInt = c.getSizeAsInt,
              scale = c.getScale,
              defaultValue = Option(c.getDefaultValue)
            )
          },

          foreignKeys = t.getForeignKeys.map { fk =>
            ForeignKey(
              name = fk.getName,
              targetTable = fk.getForeignTableName,
              references = fk.getReferences.toSeq.sortBy(_.getSequenceValue).map { r =>
                Reference(
                  source = r.getLocalColumnName,
                  target = r.getForeignColumnName
                )
              }
            )
          },

          indices = t.getIndices.map { i =>
            Index(
              name = i.getName,
              unique = i.isUnique,
              columns = i.getColumns.toSeq.sortBy(_.getOrdinalPosition).map { ic =>
                IndexColumn(
                  name = ic.getName,
                  size = 0
                )
              }
            )
          }
        )
      }
    )
  }
}
