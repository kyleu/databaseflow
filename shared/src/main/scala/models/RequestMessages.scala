package models

import java.util.UUID

sealed trait RequestMessage

case class MalformedRequest(reason: String, content: String) extends RequestMessage

case class Ping(timestamp: Long) extends RequestMessage
case object GetVersion extends RequestMessage

case class DebugInfo(data: String) extends RequestMessage

case class SubmitQuery(queryId: UUID, sql: String, action: Option[String] = None) extends RequestMessage
case class ShowTableData(queryId: UUID, name: String) extends RequestMessage
