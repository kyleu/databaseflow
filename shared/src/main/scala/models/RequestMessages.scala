package models

sealed trait RequestMessage

case class MalformedRequest(reason: String, content: String) extends RequestMessage

case class Ping(timestamp: Long) extends RequestMessage
case object GetVersion extends RequestMessage

case class DebugInfo(data: String) extends RequestMessage

case class SubmitQuery(sql: String, action: Option[String] = None) extends RequestMessage
case class ViewTable(name: String) extends RequestMessage
