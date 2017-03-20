package models

import java.util.UUID

import models.audit.AuditRecord
import models.plan.{PlanError, PlanResult}
import models.query._
import models.schema._
import models.user.UserPreferences

sealed trait ResponseMessage

case class ServerError(reason: String, content: String) extends ResponseMessage
case class VersionResponse(version: String) extends ResponseMessage

case class Pong(timestamp: Long) extends ResponseMessage
case object SendTrace extends ResponseMessage
case class DebugResponse(key: String, data: String) extends ResponseMessage
case class Disconnected(reason: String) extends ResponseMessage

case class UserSettings(userId: UUID, username: String, email: String, preferences: UserPreferences) extends ResponseMessage

case class SavedQueryResponse(savedQueries: Seq[SavedQuery], usernames: Map[UUID, String], durationMs: Int) extends ResponseMessage
case class SharedResultResponse(sharedResults: Seq[SharedResult], usernames: Map[UUID, String], durationMs: Int) extends ResponseMessage

case class SchemaResponse(schema: Schema) extends ResponseMessage
case class TableResponse(tables: Seq[Table]) extends ResponseMessage
case class ViewResponse(views: Seq[View]) extends ResponseMessage
case class ProcedureResponse(procedures: Seq[Procedure]) extends ResponseMessage

case class AuditRecordResponse(history: Seq[AuditRecord]) extends ResponseMessage
case class AuditRecordRemoved(id: Option[UUID]) extends ResponseMessage

case class TransactionStatus(state: TransactionState, occurred: Long) extends ResponseMessage

case class QueryCheckResponse(queryId: UUID, results: Seq[QueryCheckResult]) extends ResponseMessage
case class QueryResultRowCount(id: UUID, queryId: UUID, resultId: UUID, count: Int, overflow: Boolean, durationMs: Int) extends ResponseMessage
case class QueryResultResponse(id: UUID, index: Int, result: QueryResult) extends ResponseMessage
case class QueryErrorResponse(id: UUID, index: Int, error: QueryError) extends ResponseMessage
case class QueryCancelledResponse(queryId: UUID, resultId: UUID) extends ResponseMessage

case class ChartDataResponse(chartId: UUID, columns: Seq[QueryResult.Col], data: Seq[Seq[Option[String]]], durationMs: Int) extends ResponseMessage

case class PlanResultResponse(id: UUID, result: PlanResult, durationMs: Int) extends ResponseMessage
case class PlanErrorResponse(id: UUID, error: PlanError, durationMs: Int) extends ResponseMessage

case class QuerySaveResponse(savedQuery: SavedQuery, error: Option[String] = None) extends ResponseMessage
case class QueryDeleteResponse(id: UUID, error: Option[String] = None) extends ResponseMessage

case class SharedResultSaveResponse(result: SharedResult, error: Option[String] = None) extends ResponseMessage

case class RowUpdateResponse(resultId: UUID, pk: Seq[(String, String)], errors: Map[String, String] = Map.empty) extends ResponseMessage

case class ColumnDetailResponse(name: String, details: ColumnDetails) extends ResponseMessage
