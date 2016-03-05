package models

sealed trait RequestMessage

final case class MalformedRequest(reason: String, content: String) extends RequestMessage

final case class Ping(timestamp: Long) extends RequestMessage
case object GetVersion extends RequestMessage

final case class DebugInfo(data: String) extends RequestMessage
