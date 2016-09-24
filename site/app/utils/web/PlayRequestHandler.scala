package utils.web

import java.util.UUID
import javax.inject.Inject

import play.api.http._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.RequestHeader
import play.api.routing.Router
import services.audit.RequestService
import services.logging.LogService
import services.notification.RequestLogging
import utils.Logging

import scala.concurrent.Future

class PlayRequestHandler @Inject() (
    errorHandler: HttpErrorHandler,
    configuration: HttpConfiguration,
    filters: HttpFilters,
    router: Router
) extends DefaultHttpRequestHandler(router, errorHandler, configuration, filters) with Logging {

  override def routeRequest(request: RequestHeader) = {
    if (!Option(request.path).exists(_.startsWith("/assets"))) {
      log.info(s"Request from [${request.remoteAddress}]: ${request.toString()}")
      if (LogService.enabled) {
        Future { RequestService.add(RequestLogging(UUID.randomUUID, request)) }
      }
    }
    super.routeRequest(request)
  }
}
