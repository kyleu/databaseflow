import models.ui.TopFrame
import play.api._
import play.core.server.{ProdServerStart, RealServerProcess, ServerConfig, ServerProvider}
import utils.Logging

import scala.util.control.NonFatal

object DatabaseFlow extends Logging {
  def main(args: Array[String]): Unit = {
    if (args.headOption.contains("gui")) {
      System.setProperty("show.gui", "true")
    }

    val process = new RealServerProcess(args)

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
    val pidFile = ProdServerStart.createPidFile(process, config.configuration)
    val application: Application = {
      val environment = Environment(config.rootDir, process.classLoader, Mode.Prod)
      val context = ApplicationLoader.createContext(environment)
      val loader = ApplicationLoader(context)
      loader.load(context)
    }
    Play.start(application)

    val serverProvider: ServerProvider = ServerProvider.fromConfiguration(process.classLoader, config.configuration)
    val server = serverProvider.createServer(config, application)
    process.addShutdownHook {
      server.stop()
      pidFile.foreach(_.delete())
      assert(!pidFile.exists(_.exists), "PID file should not exist!")
    }
    server
  }
}
