package utils

import java.net.URLEncoder

object SlugUtils {
  def slugFor(title: String) = URLEncoder.encode(title.toLowerCase.replaceAllLiterally(" ", "-"), "utf-8")
}
