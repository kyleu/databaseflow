package services.audit

import better.files._
import java.util.UUID

import util.FileCacheService
import org.joda.time.LocalDateTime

object StartupService {
  private[this] val invalid = UUID.fromString("00000000-0000-0000-0000-000000000000")

  case class Startup(id: UUID = UUID.randomUUID, install: UUID, ip: String, occurred: LocalDateTime = new LocalDateTime())

  private[this] val f = FileCacheService.cacheDir / "startup.log"

  def list() = {
    f.createIfNotExists()

    val lines = f.lines.toSeq
    lines.map { l =>
      val split = l.split('|')
      if (split.length == 4) {
        val id = UUID.fromString(split.headOption.getOrElse(throw new IllegalStateException()))
        val occurred = new LocalDateTime(split(3))
        Startup(id = id, install = UUID.fromString(split(1)), ip = split(2), occurred = occurred)
      } else {
        Startup(install = invalid, ip = "?")
      }
    }
  }

  def add(install: UUID, ip: String) = {
    val id = UUID.randomUUID
    val s = s"$id|$install|$ip|${new LocalDateTime()}"
    f.appendLine(s)
  }
}
