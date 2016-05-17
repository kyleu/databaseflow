package controllers

import utils.ApplicationContext

import scala.concurrent.Future

@javax.inject.Singleton
class SslVerificationController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  private[this] val sslDir = {
    val root = "/home/ubuntu/ssl/webroot"
    val rootFile = new java.io.File(root)
    if (rootFile.exists && rootFile.isDirectory) {
      root
    } else {
      "./bin/ssl/webroot"
    }
  }

  def at(file: String) = withSession("ssl-verification") { implicit request =>
    if (file.contains("..")) {
      throw new IllegalStateException("No relative paths.")
    }
    val f = new java.io.File(sslDir, file)
    Future.successful(Ok.sendFile(f))
  }
}
