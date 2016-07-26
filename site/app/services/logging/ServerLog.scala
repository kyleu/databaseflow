package services.logging

import org.joda.time.LocalDateTime

final case class ServerLog(
  level: LogLevel,
  line: Int,
  logger: String,
  thread: String,
  message: String,
  occurred: LocalDateTime
)
