package controllers

import nl.grons.metrics.scala.FutureMetrics
import play.api.i18n.I18nSupport
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import utils.Logging
import utils.metrics.Instrumented

import scala.concurrent.Future

abstract class BaseController() extends Controller with I18nSupport with Instrumented with FutureMetrics with Logging {
  def act(action: String)(block: (Request[AnyContent]) => Future[Result]) = Action.async { implicit request =>
    timing(action) {
      val startTime = System.currentTimeMillis
      block(request).map { r =>
        val duration = (System.currentTimeMillis - startTime).toInt
        logRequest(request, duration, r.header.status)
        r
      }
    }
  }

  private[this] def logRequest(request: RequestHeader, duration: Int, status: Int) = {
    //val log = RequestLog(request, duration, status)
  }
}
