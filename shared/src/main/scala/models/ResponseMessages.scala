package models

import java.util.UUID

import models.plan.PlanResult
import models.query.{ QueryError, QueryResult, SavedQuery }
import models.schema.{ Procedure, Schema, Table }
import models.user.UserPreferences

sealed trait ResponseMessage

case class ServerError(reason: String, content: String) extends ResponseMessage
case class VersionResponse(version: String) extends ResponseMessage

case class InitialState(
  userId: UUID,
  username: Option[String],
  preferences: UserPreferences,
  schema: Schema,
  savedQueries: Seq[SavedQuery]
) extends ResponseMessage

case class Pong(timestamp: Long) extends ResponseMessage
case object SendTrace extends ResponseMessage
case class DebugResponse(key: String, data: String) extends ResponseMessage
case class Disconnected(reason: String) extends ResponseMessage

case class QueryResultResponse(id: UUID, result: QueryResult, durationMs: Int) extends ResponseMessage
case class QueryErrorResponse(id: UUID, error: QueryError, durationMs: Int) extends ResponseMessage

case class PlanResultResponse(id: UUID, result: PlanResult, durationMs: Int) extends ResponseMessage

case class SavedQueryResultResponse(table: Table, durationMs: Int) extends ResponseMessage
case class TableResultResponse(table: Table, durationMs: Int) extends ResponseMessage
case class ViewResultResponse(table: Table, durationMs: Int) extends ResponseMessage
case class ProcedureResultResponse(procedure: Procedure, durationMs: Int) extends ResponseMessage

case class QuerySaveResponse(savedQuery: SavedQuery, error: Option[String] = None) extends ResponseMessage
case class QueryDeleteResponse(id: UUID, error: Option[String] = None) extends ResponseMessage

case class MessageSet(messages: Seq[ResponseMessage]) extends ResponseMessage
