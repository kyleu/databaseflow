package util.web

import javax.inject.Inject

import akka.stream.Materializer
import play.api.http.Status
import util.FutureUtils.defaultContext
import play.api.mvc._
import util.Logging

import scala.concurrent.Future

class LoggingFilter @Inject() (override implicit val mat: Materializer) extends Filter with Logging {
  val prefix = "databaseflow.requests."

  val knownStatuses = Seq(
    Status.OK, Status.BAD_REQUEST, Status.FORBIDDEN, Status.NOT_FOUND,
    Status.CREATED, Status.TEMPORARY_REDIRECT, Status.INTERNAL_SERVER_ERROR, Status.CONFLICT,
    Status.UNAUTHORIZED, Status.NOT_MODIFIED
  )

  def apply(nextFilter: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
    val startTime = System.currentTimeMillis

    nextFilter(request).transform(
      result => {
        if (request.path.startsWith("/assets")) {
          result
        } else {
          val endTime = System.currentTimeMillis
          val requestTime = endTime - startTime
          log.info(s"${result.header.status} (${requestTime}ms): ${request.method} ${request.uri}")
          result.withHeaders("X-Request-Time-Ms" -> requestTime.toString)
        }
      },
      exception => exception
    )
  }
}
