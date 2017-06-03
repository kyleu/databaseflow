package utils

object FileCacheService {
  val cacheDir = {
    val prod = "/home/ubuntu/cache"
    val local = "./cache"

    if (new java.io.File(prod).exists) {
      prod
    } else if (new java.io.File(local).exists) {
      local
    } else {
      throw new IllegalStateException("Missing cache directory.")
    }
  }
}
