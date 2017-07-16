import models.ui.TopFrame
import play.api._
import play.core.server.{ProdServerStart, RealServerProcess, ServerConfig, ServerProvider}
import util.Logging

import scala.util.control.NonFatal

object DatabaseFlow extends Logging {
  def main(args: Array[String]): Unit = {
    run(args)
  }

  def run(args: Array[String]) = {
    val newArgs = if (args.headOption.contains("gui")) {
      System.setProperty("show.gui", "true")
      args.tail
    } else {
      args
    }

    val process = new RealServerProcess(newArgs)

    try {
      startServer(process)
    } catch {
      case NonFatal(x) => TopFrame.frame match {
        case Some(frame) => frame.error(x.getMessage)
        case None => process.exit(x.getMessage, Some(x))
      }
    }
  }

  def startServer(process: RealServerProcess) = {
    val config: ServerConfig = ProdServerStart.readServerConfigSettings(process)
    val application: Application = {
      val environment = Environment(config.rootDir, process.classLoader, Mode.Prod)
      val context = ApplicationLoader.createContext(environment)
      val loader = ApplicationLoader(context)
      loader.load(context)
    }
    Play.start(application)

    val serverProvider: ServerProvider = ServerProvider.fromConfiguration(process.classLoader, config.configuration)
    val server = serverProvider.createServer(config, application)
    process.addShutdownHook(server.stop())
    server
  }
}
