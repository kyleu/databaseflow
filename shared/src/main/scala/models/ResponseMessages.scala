package models

import java.util.UUID

import models.user.UserPreferences

sealed trait ResponseMessage

final case class ServerError(reason: String, content: String) extends ResponseMessage
final case class VersionResponse(version: String) extends ResponseMessage

final case class InitialState(userId: UUID, username: Option[String], preferences: UserPreferences) extends ResponseMessage

final case class Pong(timestamp: Long) extends ResponseMessage
case object SendTrace extends ResponseMessage
final case class DebugResponse(key: String, data: String) extends ResponseMessage
final case class Disconnected(reason: String) extends ResponseMessage

final case class MessageSet(messages: Seq[ResponseMessage]) extends ResponseMessage
