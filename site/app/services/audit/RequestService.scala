package services.audit

import java.io.{BufferedWriter, FileWriter}

import util.FileCacheService
import play.api.libs.json.Json
import services.notification.RequestLogging.RequestLog

import scala.io.Source

object RequestService {
  private[this] val requestFile = FileCacheService.cacheDir + "/request.log"
  private[this] val output = new BufferedWriter(new FileWriter(requestFile, true))

  def list() = {
    val f = new java.io.File(requestFile)
    if (!f.exists) {
      f.createNewFile()
    }

    val lines = Source.fromFile(f).getLines.toSeq
    lines.map { l =>
      Json.parse(l)
    }
  }

  def add(request: RequestLog) = {
    import services.notification.RequestLogging.jsonFmt
    val json = Json.toJson(request)
    output.write(json.toString + '\n')
    output.flush()
  }
}
