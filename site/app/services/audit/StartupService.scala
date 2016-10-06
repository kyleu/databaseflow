package services.audit

import java.io.{BufferedWriter, FileWriter}
import java.util.UUID

import cache.FileCacheService
import org.joda.time.LocalDateTime

import scala.io.Source

object StartupService {
  case class Startup(id: UUID = UUID.randomUUID, ip: String, license: UUID, occurred: LocalDateTime = new LocalDateTime())

  private[this] val startupFile = FileCacheService.cacheDir + "/startup.log"
  private[this] val output = new BufferedWriter(new FileWriter(startupFile, true))
  private[this] val trialId = UUID.fromString("00000000-0000-0000-0000-000000000000")

  def list() = {
    val f = new java.io.File(startupFile)
    if (!f.exists) {
      f.createNewFile()
    }

    val lines = Source.fromFile(f).getLines.toSeq
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
    output.write(s + '\n')
    output.flush()
  }
}
