package models.result

import java.util.UUID

import models.query.QueryResult
import models.schema.ColumnType.LongType
import services.result.CachedResultService
import services.schema.SchemaService

object CachedResultInsert {
  def insert(result: CachedResult, columns: Seq[QueryResult.Col], containsRowNum: Boolean) = {
    val newColumns = result.source match {
      case Some(src) => withRelations(columns, result.connectionId, src)
      case None => columns
    }

    val columnsPlus = if (containsRowNum) {
      newColumns
    } else {
      QueryResult.Col("#", LongType, None, None) +: newColumns
    }

    CachedResultService.insertCacheResult(result.copy(columns = columnsPlus.size))
    columnsPlus
  }

  private[this] def withRelations(columns: Seq[QueryResult.Col], connectionId: UUID, src: String) = SchemaService.getTable(connectionId, src) match {
    case Some(t) => columns.map { col =>
      t.foreignKeys.find(_.references.exists(_.source == col.name)) match {
        case Some(fk) => col.copy(
          relationTable = Some(fk.targetTable),
          relationColumn = fk.references.find(_.source == col.name).map(_.target)
        )
        case None => col
      }
    }
    case None => columns
  }
}
