import models.{ InitialState, Pong, ResponseMessage }
import utils.Logging

trait MessageHelper { this: DatabaseFlow =>
  protected[this] def handleMessage(rm: ResponseMessage) = rm match {
    case p: Pong => latencyMs = Some((System.currentTimeMillis - p.timestamp).toInt)
    case is: InitialState => onInitialState(is)
    case _ => Logging.info("Received: " + rm.getClass.getSimpleName)
  }
}
