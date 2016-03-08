package services.connection

import models._
import play.api.libs.json.{ JsObject, Json }
import utils.metrics.InstrumentedActor

trait ConnectionServiceTraceHelper extends InstrumentedActor { this: ConnectionService =>
  protected[this] def handleConnectionTrace() {
    val ret = ConnectionTraceResponse(id, user.id, currentUsername)
    sender() ! ret
  }

  protected[this] def handleClientTrace() {
    pendingDebugChannel = Some(sender())
    out ! SendTrace
  }

  protected[this] def handleDebugInfo(data: String) = pendingDebugChannel match {
    case Some(dc) =>
      val json = Json.parse(data).as[JsObject]
      dc ! ClientTraceResponse(id, json)
    case None =>
      log.warn(s"Received unsolicited DebugInfo [$data] from [$id] with no active brawl.")
  }
}
