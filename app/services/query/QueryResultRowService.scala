package services.query

import java.util.UUID

import models.query.{QueryResult, RowDataOptions}
import models.result.QueryResultRow
import models.user.User
import utils.FutureUtils.defaultContext

object QueryResultRowService {
  def getTableDataWhereClause(user: User, connectionId: UUID, name: String, whereClause: String, values: Seq[Any]) = {
    RowDataService.getRowDataWhereClause(user, connectionId, name, whereClause, values).map(transform)
  }

  def getTableData(user: User, connectionId: UUID, name: String, rdo: RowDataOptions) = {
    RowDataService.getRowData(user, connectionId, "table", name, rdo).map(transform)
  }

  def getViewData(user: User, connectionId: UUID, name: String, rdo: RowDataOptions) = {
    RowDataService.getRowData(user, connectionId, "view", name, rdo).map(transform)
  }

  private[this] def transform(result: QueryResult) = {
    val columns = result.columns.map(_.name)
    result.data.map { row =>
      QueryResultRow(columns, row)
    }
  }
}
