package services.scalaexport.inject

import better.files.File
import models.scalaexport.ExportResult

import scala.util.Random

object InjectIcons {
  private[this] val icons = IndexedSeq(
    "folder-o", "folder-open-o", "frown-o", "futbol-o", "gamepad", "gavel", "gift", "glass", "globe", "graduation-cap",
    "hand-lizard-o", "hand-paper-o", "hand-peace-o", "hand-pointer-o", "hand-rock-o", "hand-scissors-o", "hand-spock-o", "handshake-o",
    "hashtag", "hdd-o", "headphones", "heart", "heart-o", "heartbeat", "history", "home", "hourglass", "hourglass-o", "hourglass-start",
    "i-cursor", "id-badge", "id-card", "id-card-o", "inbox", "industry", "info", "info-circle", "key", "keyboard-o",
    "language", "laptop", "leaf", "lemon-o", "level-down", "level-up", "life-ring", "lightbulb-o", "line-chart", "location-arrow",
    "lock", "low-vision", "magic", "magnet", "male", "map", "map-marker", "map-o", "map-pin", "map-signs"
  )

  private[this] def randomIcon = icons(Random.nextInt(icons.size))

  def inject(result: ExportResult, rootDir: File) = {
    def iconFieldsFor(s: String) = {
      val startString = "  // Start model icons"
      val startIndex = s.indexOf(startString)
      val newContent = result.models.flatMap { m =>
        s.indexOf("val " + m.propertyName + " = ") match {
          case x if x > -1 && x < startIndex => None
          case _ => Some(s"""  val ${m.propertyName} = "fa-${m.icon.getOrElse(randomIcon)}"""")
        }
      }.sorted.mkString("\n")
      InjectHelper.replaceBetween(original = s, start = startString, end = "  // End model icons", newContent = newContent)
    }

    val iconSourceFile = rootDir / "shared" / "src" / "main" / "scala" / "models" / "template" / "Icons.scala"
    val newContent = iconFieldsFor(iconSourceFile.contentAsString)
    iconSourceFile.overwrite(newContent)

    "Icons.scala" -> newContent
  }
}
