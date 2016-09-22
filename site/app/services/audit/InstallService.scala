package services.audit

import java.io.{BufferedWriter, FileWriter}
import java.util.UUID

import cache.FileCacheService
import org.joda.time.LocalDateTime

import scala.io.Source

object InstallService {
  case class Install(id: UUID = UUID.randomUUID, ip: String, note: String, occurred: LocalDateTime = new LocalDateTime())

  private[this] val installFile = FileCacheService.cacheDir + "/install.log"
  private[this] val output = new BufferedWriter(new FileWriter(installFile, true))

  def list() = {
    val f = new java.io.File(installFile)
    if (!f.exists) {
      f.createNewFile()
    }

    val lines = Source.fromFile(f).getLines.toSeq
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
    output.write(s + '\n')
    output.flush()
  }
}
