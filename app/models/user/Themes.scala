package models.user

import scala.util.Random

object Themes {
  // TODO Darken most of these.
  val all = Seq(
    "red" -> "#f44336",
    "pink" -> "#e91e63",
    "purple" -> "#9c27b0",
    "deep-purple" -> "#673ab7",
    "indigo" -> "#3f51b5",
    "blue" -> "#2196f3",
    "light-blue" -> "#03a9f4",
    "cyan" -> "#00bcd4",
    "teal" -> "#009688",
    "green" -> "#4caf50",
    "light-green" -> "#8bc34a",
    "amber" -> "#ffc107",
    "orange" -> "#ff9800",
    "deep-orange" -> "#ff5722",
    "brown" -> "#795548",
    "grey" -> "#9e9e9e",
    "blue-grey" -> "#607d8b",
    "black" -> "#333333"
  )

  val map = all.toMap

  def getRandom = all(Random.nextInt(all.size))
}
