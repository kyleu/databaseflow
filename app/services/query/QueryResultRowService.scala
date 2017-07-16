package services.query

import java.util.UUID

import models.query.{QueryResult, RowDataOptions}
import models.result.QueryResultRow
import models.user.User
import util.FutureUtils.defaultContext

object QueryResultRowService {
  def getTableData(user: User, connectionId: UUID, name: String, columns: Seq[String], rdo: RowDataOptions) = {
    RowDataService.getRowData(user, connectionId, QueryResult.SourceType.Table, name, columns, rdo).map(transform)
  }

  def getViewData(user: User, connectionId: UUID, name: String, columns: Seq[String], rdo: RowDataOptions) = {
    RowDataService.getRowData(user, connectionId, QueryResult.SourceType.View, name, columns, rdo).map(transform)
  }

  private[this] def transform(result: QueryResult) = {
    val columns = result.columns.map(_.name)
    result.data.map { row =>
      QueryResultRow(columns, row)
    }
  }
}
