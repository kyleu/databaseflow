package utils.web

import java.net.URI

import gui.web.WebApp
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{ Mode, Play }
import play.core.server.{ NettyServer, ServerConfig }

class WebApplication() extends WebApp {
  private[this] lazy val app = new GuiceApplicationBuilder().build()

  private[this] var server: Option[NettyServer] = None

  var _started = false

  def started = _started

  def start() = {
    Play.start(app)
    server = Some(NettyServer.fromApplication(
      application = app,
      config = ServerConfig(
        port = Some(4000),
        sslPort = Some(4443),
        mode = Mode.Prod
      )
    ))
    _started = true
  }

  def stop() = if (started) {
    Play.stop(app)
    server.foreach(_.stop)
    server = None
    _started = false
  }
}
