package models

import java.util.UUID

import models.plan.PlanNode
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

object QueryResult {
  case class Col(name: String, t: String)
}
case class QueryResult(id: UUID, sql: String, columns: Seq[QueryResult.Col], data: Seq[Seq[Option[String]]], durationMs: Int) extends ResponseMessage
case class QueryError(id: UUID, sql: String, code: String, message: String, line: Int, position: Int, durationMs: Int) extends ResponseMessage
case class PlanResult(id: UUID, name: String, sql: String, asText: String, node: PlanNode, created: Long) extends ResponseMessage

case class MessageSet(messages: Seq[ResponseMessage]) extends ResponseMessage
