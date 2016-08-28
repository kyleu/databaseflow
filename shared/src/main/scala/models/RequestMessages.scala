package models

import java.util.UUID

import models.query.{QueryResult, RowDataOptions, SavedQuery, SharedResult}

sealed trait RequestMessage

case class MalformedRequest(reason: String, content: String) extends RequestMessage

case class Ping(timestamp: Long) extends RequestMessage
case object GetVersion extends RequestMessage
case class DebugInfo(data: String) extends RequestMessage

case object RefreshSchema extends RequestMessage

case class GetTableDetail(name: String) extends RequestMessage
case class GetProcedureDetail(name: String) extends RequestMessage
case class GetViewDetail(name: String) extends RequestMessage

case object BeginTransaction extends RequestMessage
case object RollbackTransaction extends RequestMessage
case object CommitTransaction extends RequestMessage

case class CheckQuery(queryId: UUID, sql: String) extends RequestMessage
case class SubmitQuery(queryId: UUID, sql: String, action: Option[String] = None, resultId: UUID) extends RequestMessage
case class GetRowData(key: String, queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) extends RequestMessage
case class CancelQuery(queryId: UUID, resultId: UUID) extends RequestMessage
case class CloseQuery(queryId: UUID) extends RequestMessage

case class ChartDataRequest(chartId: UUID, source: QueryResult.Source) extends RequestMessage

case class QuerySaveRequest(query: SavedQuery) extends RequestMessage
case class QueryDeleteRequest(id: UUID) extends RequestMessage

case class SharedResultSaveRequest(result: SharedResult) extends RequestMessage

case class GetQueryHistory(limit: Int = 100, offset: Int = 0) extends RequestMessage
case class InsertAuditHistory(id: UUID) extends RequestMessage
case class RemoveAuditHistory(id: Option[UUID]) extends RequestMessage

case class CreateSampleDatabase(queryId: UUID) extends RequestMessage
