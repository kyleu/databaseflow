package models

import java.util.UUID

import models.query.{ RowDataOptions, SavedQuery }

sealed trait RequestMessage

case class MalformedRequest(reason: String, content: String) extends RequestMessage

case class Ping(timestamp: Long) extends RequestMessage
case object GetVersion extends RequestMessage
case class DebugInfo(data: String) extends RequestMessage

case object RefreshSchema extends RequestMessage

case class GetTableDetail(name: String) extends RequestMessage
case class GetProcedureDetail(name: String) extends RequestMessage
case class GetViewDetail(name: String) extends RequestMessage

case class CheckQuery(queryId: UUID, sql: String) extends RequestMessage
case class SubmitQuery(queryId: UUID, sql: String, action: Option[String] = None, resultId: UUID) extends RequestMessage
case class GetTableRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) extends RequestMessage
case class GetViewRowData(queryId: UUID, name: String, options: RowDataOptions, resultId: UUID) extends RequestMessage
case class CancelQuery(queryId: UUID, resultId: UUID) extends RequestMessage

case class QuerySaveRequest(query: SavedQuery) extends RequestMessage
case class QueryDeleteRequest(id: UUID) extends RequestMessage

case class CreateSampleDatabase(queryId: UUID) extends RequestMessage
