package services.notification

import java.util.UUID

import org.joda.time.LocalDateTime
import play.api.libs.json.Json
import play.api.mvc.RequestHeader

object RequestLogging {
  case class RequestLog(
    id: UUID,
    tags: Map[String, String],
    version: String,
    remoteAddress: String,
    secure: Boolean,
    host: String,
    method: String,
    path: String,
    queryString: String,
    headers: Map[String, String],
    occurred: String
  )

  implicit val jsonFmt = Json.format[RequestLog]

  private[this] val dateFmt = org.joda.time.format.ISODateTimeFormat.dateTime()

  def apply(id: UUID, request: RequestHeader) = {
    RequestLog(
      id = id,
      tags = request.tags,
      version = request.version,
      remoteAddress = request.remoteAddress,
      secure = request.secure,
      host = request.host,
      method = request.method,
      path = request.path,
      queryString = request.rawQueryString,
      headers = request.headers.headers.filterNot(_._1 == "Cookie").toMap,
      occurred = dateFmt.print(new LocalDateTime())
    )
  }
}
