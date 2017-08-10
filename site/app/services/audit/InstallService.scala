package services.audit

import better.files._
import java.util.UUID

import util.FileCacheService
import org.joda.time.LocalDateTime

object InstallService {
  case class Install(id: UUID = UUID.randomUUID, ip: String, note: String, occurred: LocalDateTime = new LocalDateTime())

  private[this] val f = FileCacheService.cacheDir / "install.log"

  def list() = {
    f.createIfNotExists()

    val lines = f.lines.toSeq
    lines.map { l =>
      val split = l.split('|')
      if (split.length == 4) {
        val id = UUID.fromString(split.headOption.getOrElse(throw new IllegalStateException()))
        val occurred = new LocalDateTime(split(3))
        Install(id = id, ip = split(1), note = split(2), occurred = occurred)
      } else {
        Install(ip = "?", note = s"Error, [${split.length}] fields.")
      }
    }
  }

  def add(ip: String, note: String) = {
    val id = UUID.randomUUID
    val s = s"$id|$ip|$note|${new LocalDateTime()}"
    f.appendLine(s)
  }
}
