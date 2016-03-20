package models

import java.util.UUID

import models.plan.{ PlanResult, PlanNode }
import models.query.{ QueryResult, QueryError }
import models.schema.Schema
import models.user.UserPreferences

sealed trait ResponseMessage

case class ServerError(reason: String, content: String) extends ResponseMessage
case class VersionResponse(version: String) extends ResponseMessage

case class InitialState(userId: UUID, username: Option[String], preferences: UserPreferences, schema: Schema) extends ResponseMessage

case class Pong(timestamp: Long) extends ResponseMessage
case object SendTrace extends ResponseMessage
case class DebugResponse(key: String, data: String) extends ResponseMessage
case class Disconnected(reason: String) extends ResponseMessage

case class QueryResultResponse(id: UUID, result: QueryResult, durationMs: Int) extends ResponseMessage
case class QueryErrorResponse(id: UUID, error: QueryError, durationMs: Int) extends ResponseMessage
case class PlanResultResponse(id: UUID, result: PlanResult, durationMs: Int) extends ResponseMessage

case class MessageSet(messages: Seq[ResponseMessage]) extends ResponseMessage
