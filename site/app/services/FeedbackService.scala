package services

import java.nio.file.{Files, Paths}
import java.util.{Base64, UUID}

object FeedbackService {
  case class Feedback(id: UUID, email: String, content: String)

  private[this] val feedbackDir = "./tmp/feedback/"

  def list() = {
    val dir = new java.io.File(feedbackDir)
    dir.listFiles.filter(_.getName.endsWith(".feedback")).map(x => UUID.fromString(x.getName.stripSuffix(".feedback")))
  }

  def load(id: UUID) = {
    import scala.collection.JavaConverters._

    val filename = id + ".feedback"
    val file = Paths.get(feedbackDir, filename)
    if (Files.exists(file)) {
      val lines = Files.readAllLines(file).asScala
      Feedback(id, lines.headOption.getOrElse(""), lines.tail.mkString("\n"))
    } else {
      throw new IllegalArgumentException(s"Feedback already exists for [$id].")
    }
  }

  def save(feedback: Feedback, overwrite: Boolean = false) = {
    val filename = feedback.id + ".feedback"
    val file = Paths.get(feedbackDir, filename)
    if ((!overwrite) && Files.exists(file)) {
      throw new IllegalArgumentException(s"Feedback already exists for [${feedback.id}] and cannot be overwritten.")
    } else {
      val text = feedback.email + "\n" + feedback.content
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
