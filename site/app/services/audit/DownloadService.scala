package services.audit

import java.io.{BufferedWriter, FileWriter}
import java.util.UUID

import cache.FileCacheService
import org.joda.time.LocalDateTime

import scala.io.Source

object DownloadService {
  case class Download(id: UUID = UUID.randomUUID, ip: String, platform: String, occurred: LocalDateTime = new LocalDateTime())

  private[this] val downloadFile = FileCacheService.cacheDir + "/download.log"
  private[this] val output = new BufferedWriter(new FileWriter(downloadFile, true))

  def list() = {
    val f = new java.io.File(downloadFile)
    if (!f.exists) {
      f.createNewFile()
    }

    val lines = Source.fromFile(f).getLines.toSeq
    lines.map { l =>
      val split = l.split('|')
      if (split.length == 4) {
        val id = UUID.fromString(split.headOption.getOrElse(throw new IllegalStateException()))
        val occurred = new LocalDateTime(split(3))
        Download(id = id, ip = split(1), platform = split(2), occurred = occurred)
      } else {
        Download(ip = "?", platform = "error")
      }
    }
  }

  def add(ip: String, platform: String) = {
    val id = UUID.randomUUID
    val s = s"$id|$ip|$platform|${new LocalDateTime()}"
    output.write(s + '\n')
    output.flush()
  }
}
