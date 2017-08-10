package util

import better.files._

object FileCacheService {
  val cacheDir = {
    val prod = "/home/ubuntu/cache"
    val local = "./cache"

    if (prod.toFile.isDirectory) {
      prod
    } else if (local.toFile.isDirectory) {
      local
    } else {
      throw new IllegalStateException(s"Missing cache directory. Tried [$prod, $local].")
    }
  }
}
