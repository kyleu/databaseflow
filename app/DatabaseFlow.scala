import play.api._
import play.core.server.{ProdServerStart, RealServerProcess, ServerConfig, ServerProvider}
import util.Logging

object DatabaseFlow extends Logging {
  def main(args: Array[String]): Unit = startServer(new RealServerProcess(args))

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
