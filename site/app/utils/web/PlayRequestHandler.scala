package utils.web

import javax.inject.Inject

import play.api.Logger
import play.api.http._
import play.api.mvc.RequestHeader
import play.api.routing.Router

class PlayRequestHandler @Inject() (
    errorHandler: HttpErrorHandler,
    configuration: HttpConfiguration,
    filters: HttpFilters,
    router: Router
) extends DefaultHttpRequestHandler(router, errorHandler, configuration, filters) {

  override def routeRequest(request: RequestHeader) = {
    if (!Option(request.path).exists(_.startsWith("/assets"))) {
      Logger.info(s"Request from [${request.remoteAddress}]: ${request.toString()}")
    }
    super.routeRequest(request)
  }
}
