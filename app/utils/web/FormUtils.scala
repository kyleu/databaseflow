package utils.web

import play.api.mvc.{AnyContent, Request}

object FormUtils {
  def getForm(request: Request[AnyContent]) = request.body.asFormUrlEncoded match {
    case Some(f) => f.mapValues(x => x.headOption.getOrElse(throw new IllegalStateException("Empty form element.")))
    case None => throw new IllegalStateException("Missing form post.")
  }
}
