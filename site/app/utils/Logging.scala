package utils

import org.slf4j.LoggerFactory

trait Logging {
  protected[this] val log = {
    val name = s"databaseflow.${this.getClass.getSimpleName.replace("$", "")}"
    LoggerFactory.getLogger(name)
  }
}
