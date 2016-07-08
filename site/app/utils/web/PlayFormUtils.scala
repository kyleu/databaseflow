package utils.web

import play.api.data.FormError

object PlayFormUtils {
  def errorsToString(errors: Seq[FormError]) = errors.map(e => e.key + ": " + e.message).mkString(", ")
}
