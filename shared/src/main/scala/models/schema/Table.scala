package models.schema

import java.util.UUID

case class Table(
  name: String,
  connection: UUID,
  catalog: Option[String],
  schema: Option[String],
  description: Option[String],
  definition: Option[String],

  storageEngine: Option[String] = None,

  rowCountEstimate: Option[Long] = None,
  averageRowLength: Option[Int] = None,
  dataLength: Option[Long] = None,

  columns: Seq[Column] = Nil,
  rowIdentifier: Seq[String] = Nil,
  primaryKey: Option[PrimaryKey] = None,
  foreignKeys: Seq[ForeignKey] = Nil,
  indexes: Seq[Index] = Nil,

  createTime: Option[Long] = None,
  loadedAt: Long = System.currentTimeMillis
)
