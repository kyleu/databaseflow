package utils

import services.config.ConfigFileService
import utils.FutureUtils.defaultContext

import scala.concurrent.Future

trait ApplicationHelper { this: ApplicationContext =>
  def startupComplete(debug: Boolean) = {
    if ((!debug) && java.awt.Desktop.isDesktopSupported) {
      Future {
        Thread.sleep(2000)
        java.awt.Desktop.getDesktop.browse(new java.net.URI("http://localhost:4260"))
      }
    }

    log.warn(s"${utils.Config.projectName} started.")
    if (ConfigFileService.isDocker) {
      log.warn(" - Head to http://[docker address]:4260 to get started!")
      log.warn(" - Since this is a docker container, you'll need to expose port 4260, by using the command flag [-p 4260:4260].")
    } else {
      log.warn(" - Head to http://localhost:4260 to get started!")
    }
  }
}
