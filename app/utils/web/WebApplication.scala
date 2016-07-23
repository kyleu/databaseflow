package utils.web

import java.net.URI

import gui.web.WebApp
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.{Mode, Play}
import play.core.server.{NettyServer, ServerConfig}
import services.data.MasterDdl
import services.database.core.MasterDatabase
import services.licensing.LicenseService
import services.supervisor.ActorSupervisor

class WebApplication() extends WebApp {
  private[this] lazy val app = new GuiceApplicationBuilder().build()

  private[this] var server: Option[NettyServer] = None

  var _started = false

  def started = _started

  def start() = {
    val ws = app.injector.instanceOf(classOf[WSClient])
    ActorSupervisor.startIfNeeded(ws)

    val address = if (LicenseService.isPersonalEdition) { "127.0.0.1" } else { "0.0.0.0" }

    Play.start(app)
    server = Some(NettyServer.fromApplication(
      application = app,
      config = ServerConfig(
        port = Some(4260),
        sslPort = Some(4263),
        address = address,
        mode = Mode.Prod
      )
    ))
    _started = true

    if (java.awt.Desktop.isDesktopSupported) {
      java.awt.Desktop.getDesktop.browse(new URI("http://localhost:4260"))
    }
  }

  def stop() = if (started) {
    Play.stop(app)
    server.foreach(_.stop)
    server = None
    _started = false
  }
}
