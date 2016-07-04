package utils.web

import gui.web.WebApp
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Mode, Play}
import play.core.server.{NettyServer, ServerConfig}
import services.licensing.LicenseService

class WebApplication() extends WebApp {
  private[this] lazy val app = new GuiceApplicationBuilder().build()

  private[this] var server: Option[NettyServer] = None

  var _started = false

  def started = _started

  def start() = {
    if (!LicenseService.hasLicense) {
      LicenseService.readLicense()
    }

    val address = if (LicenseService.isPersonalEdition) { "127.0.0.1" } else { "0.0.0.0" }

    Play.start(app)
    server = Some(NettyServer.fromApplication(
      application = app,
      config = ServerConfig(
        port = Some(4000),
        sslPort = Some(4443),
        address = address,
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
