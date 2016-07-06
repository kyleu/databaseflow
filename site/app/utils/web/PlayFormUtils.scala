package utils.web

import play.api.data.FormError

object PlayFormUtils {
  def errorsToString(errors: Seq[FormError]) = errors.mkString(", ")
}
