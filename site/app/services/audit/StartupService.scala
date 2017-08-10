package services.audit

import better.files._
import java.util.UUID

import util.FileCacheService
import org.joda.time.LocalDateTime

import scala.io.Source

object StartupService {
  case class Startup(id: UUID = UUID.randomUUID, ip: String, license: UUID, occurred: LocalDateTime = new LocalDateTime())

  private[this] val f = FileCacheService.cacheDir / "startup.log"
  private[this] val trialId = UUID.fromString("00000000-0000-0000-0000-000000000000")

  def list() = {
    f.createIfNotExists()

    val lines = f.lines.toSeq
    lines.map { l =>
      val split = l.split('|')
      if (split.length == 4) {
        val id = UUID.fromString(split.headOption.getOrElse(throw new IllegalStateException()))
        val occurred = new LocalDateTime(split(3))
        Startup(id = id, ip = split(1), license = UUID.fromString(split(2)), occurred = occurred)
      } else {
        Startup(ip = "?", license = trialId)
      }
    }
  }

  def add(ip: String, license: Option[UUID]) = {
    val id = UUID.randomUUID
    val s = s"$id|$ip|${license.getOrElse(trialId)}|${new LocalDateTime()}"
    f.appendLine(s)
  }
}
