package web

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{ Mode, Play }
import play.core.server.{ NettyServer, ServerConfig }

class WebApplication() {
  private[this] lazy val app = new GuiceApplicationBuilder().build()

  private[this] lazy val server = NettyServer.fromApplication(
    application = app,
    config = ServerConfig(
      port = Some(9000),
      sslPort = Some(9443),
      mode = Mode.Prod
    )
  )

  var _started = false

  def started = _started

  def start() = {
    Play.start(app)
  }

  def stop() = if (started) {
    Play.stop(app)
  }
}
