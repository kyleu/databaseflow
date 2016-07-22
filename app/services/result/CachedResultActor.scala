package services.result

import akka.actor.Props
import org.joda.time.LocalDateTime
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import utils.Logging
import utils.metrics.InstrumentedActor

import scala.concurrent.duration._

object CachedResultActor {
  case class Cleanup(since: Option[LocalDateTime])
  def props() = Props(classOf[CachedResultActor])
}

class CachedResultActor() extends InstrumentedActor with Logging {
  override def preStart() = {
    log.info("Query result cache cleanup is scheduled to run every ten minutes.")
    context.system.scheduler.schedule(10.minutes, 10.minutes, self, CachedResultActor.Cleanup(None))
  }

  override def receiveRequest = {
    case c: CachedResultActor.Cleanup =>
      val ret = CachedResultService.cleanup(c.since.getOrElse(new LocalDateTime().minusDays(2)))
      log.info(ret.toString)
      sender() ! ret
  }
}
