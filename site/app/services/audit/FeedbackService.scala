package services.audit

import java.nio.file.{Files, Paths}
import java.util.UUID

import cache.FileCacheService
import org.joda.time.LocalDateTime

object FeedbackService {
  case class Feedback(id: UUID, from: String, content: String, version: Int, occurred: LocalDateTime = new LocalDateTime())

  private[this] val feedbackDir = FileCacheService.cacheDir + "/feedback"

  def list() = {
    val dir = new java.io.File(feedbackDir)
    if (!dir.isDirectory) {
      throw new IllegalStateException(s"Missing feedback directory [$feedbackDir].")
    }

    dir.listFiles.filter(_.getName.endsWith(".feedback")).map(x => UUID.fromString(x.getName.stripSuffix(".feedback")))
  }

  def load(id: UUID) = {
    import scala.collection.JavaConverters._

    val filename = id + ".feedback"
    val file = Paths.get(feedbackDir, filename)
    if (Files.exists(file)) {
      val lines = Files.readAllLines(file).asScala
      val name = lines.headOption.getOrElse("")
      val content = lines.tail.dropRight(1).mkString("\n")
      val last = lines.tail.lastOption.getOrElse("0:0").split(':')
      val version = last.headOption.getOrElse("0").toInt
      val occurred = new LocalDateTime(last.lastOption.getOrElse("0").toLong)
      Feedback(id, name, content, version, occurred)
    } else {
      throw new IllegalArgumentException(s"Feedback does not exist for [$id].")
    }
  }

  def save(feedback: Feedback, overwrite: Boolean = false) = {
    val filename = feedback.id + ".feedback"
    val file = Paths.get(feedbackDir, filename)
    if ((!overwrite) && Files.exists(file)) {
      throw new IllegalArgumentException(s"Feedback already exists for [${feedback.id}] and cannot be overwritten.")
    } else {
      val text = feedback.from + "\n" + feedback.content + "\nv1"
      Files.write(file, text.getBytes)
    }
  }

  def remove(id: UUID) = {
    val filename = id + ".feedback"
    val file = Paths.get(feedbackDir, filename)
    if (Files.exists(file)) {
      Files.delete(file)
    } else {
      throw new IllegalArgumentException(s"No feedback available for [$id].")
    }
  }
}
