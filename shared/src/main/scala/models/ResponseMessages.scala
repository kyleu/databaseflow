package models

import java.util.UUID

import models.plan.{ PlanError, PlanResult }
import models.query.{ QueryError, QueryResult, SavedQuery }
import models.schema.{ Procedure, Schema, Table, View }
import models.user.UserPreferences

sealed trait ResponseMessage

case class ServerError(reason: String, content: String) extends ResponseMessage
case class VersionResponse(version: String) extends ResponseMessage

case class Pong(timestamp: Long) extends ResponseMessage
case object SendTrace extends ResponseMessage
case class DebugResponse(key: String, data: String) extends ResponseMessage
case class Disconnected(reason: String) extends ResponseMessage

case class UserSettings(userId: Option[UUID], username: Option[String], email: Option[String], preferences: Option[UserPreferences]) extends ResponseMessage

case class SavedQueryResultResponse(savedQueries: Seq[SavedQuery], durationMs: Int) extends ResponseMessage

case class SchemaResultResponse(schema: Schema) extends ResponseMessage
case class TableResultResponse(tables: Seq[Table]) extends ResponseMessage
case class ViewResultResponse(views: Seq[View]) extends ResponseMessage
case class ProcedureResultResponse(procedures: Seq[Procedure]) extends ResponseMessage

case class QueryCheckResponse(queryId: UUID, error: Option[String] = None, line: Option[Int] = None, position: Option[Int] = None) extends ResponseMessage
case class QueryResultRowCount(id: UUID, queryId: UUID, count: Int, durationMs: Int) extends ResponseMessage
case class QueryResultResponse(id: UUID, result: QueryResult, durationMs: Int) extends ResponseMessage
case class QueryErrorResponse(id: UUID, error: QueryError, durationMs: Int) extends ResponseMessage
case class QueryCancelledResponse(queryId: UUID, resultId: UUID) extends ResponseMessage

case class PlanResultResponse(id: UUID, result: PlanResult, durationMs: Int) extends ResponseMessage
case class PlanErrorResponse(id: UUID, error: PlanError, durationMs: Int) extends ResponseMessage

case class BatchQueryStatus(id: UUID, completedQueries: Int, remainingQueries: Int, durationMs: Int) extends ResponseMessage

case class QuerySaveResponse(savedQuery: SavedQuery, error: Option[String] = None) extends ResponseMessage
case class QueryDeleteResponse(id: UUID, error: Option[String] = None) extends ResponseMessage

case class MessageSet(messages: Seq[ResponseMessage]) extends ResponseMessage
