package models.user

import scala.util.Random

object Themes {
  val all = Seq(
    "red", "pink", "purple", "deep-purple", "indigo", "blue",
    "light-blue", "cyan", "teal", "green", "light-green", "lime",
    "yellow", "amber", "orange", "deep-orange", "brown", "grey", "blue-grey"
  )

  def getRandom = all(Random.nextInt(all.size))
}
