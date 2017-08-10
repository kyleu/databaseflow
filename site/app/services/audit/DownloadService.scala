package services.audit

import better.files._
import java.io.{BufferedWriter, FileWriter}
import java.util.UUID

import util.FileCacheService
import org.joda.time.LocalDateTime

object DownloadService {
  case class Download(id: UUID = UUID.randomUUID, ip: String, platform: String, occurred: LocalDateTime = new LocalDateTime())

  private[this] val downloadFile = FileCacheService.cacheDir + "/download.log"
  private[this] val output = new BufferedWriter(new FileWriter(downloadFile, true))

  def list() = {
    val f = downloadFile.toFile
    f.createIfNotExists()

    val lines = f.lines.toSeq
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
