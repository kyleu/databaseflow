package com.databaseflow.services.scalaexport.db.inject

object InjectHelper {
  def replaceBetween(original: String, start: String, end: String, newContent: String) = {
    val startIndex = original.indexOf(start)
    if (startIndex == -1) {
      throw new IllegalStateException(s"Cannot inject. No start key matching [$start].")
    }
    val endIndex = original.indexOf(end)
    if (endIndex == -1) {
      throw new IllegalStateException(s"Cannot inject. No end key matching [$end].")
    }

    original.substring(0, startIndex + start.length) + "\n" + newContent + "\n" + original.substring(endIndex)
  }
}
