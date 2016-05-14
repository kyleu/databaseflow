package utils

import scala.util.Random

object FileUtils {
  @scala.annotation.tailrec
  def getTempFile(name: String, extension: String): java.io.File = {
    val ret = new java.io.File("./tmp", s"$name.$extension")

    if (!ret.exists()) {
      ret
    } else {
      getTempFile(name + Random.alphanumeric.take(1), extension)
    }
  }
}
