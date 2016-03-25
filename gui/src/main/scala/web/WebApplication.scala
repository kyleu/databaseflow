package web

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.routing.Router
import play.api.{ Mode, Play }
import play.core.server.{ NettyServer, ServerConfig }

class WebApplication() {
  val app = new GuiceApplicationBuilder().build()
  Play.start(app)

  println(app.injector.instanceOf[Router].documentation.mkString("\n"))

  val server = NettyServer.fromApplication(
    application = app,
    config = ServerConfig(
      port = Some(9000),
      sslPort = Some(9443),
      mode = Mode.Prod
    )
  )
}
