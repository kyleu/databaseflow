package util

import org.slf4j.LoggerFactory
import play.api.Logger

trait Logging {
  protected[this] val log = new Logger(LoggerFactory.getLogger(getClass))
}
