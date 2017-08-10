package services.audit

import better.files._
import play.api.libs.json.Json
import services.notification.RequestLogging.RequestLog
import util.FileCacheService

object RequestService {
  private[this] val f = FileCacheService.cacheDir / "request.log"

  def list() = {
    f.createIfNotExists()

    val lines = f.lines.toSeq
    lines.map { l =>
      Json.parse(l)
    }
  }

  def add(request: RequestLog) = {
    import services.notification.RequestLogging.jsonFmt
    val json = Json.toJson(request)
    f.appendLine(json.toString)
  }
}
