import utils.web.WebApplication

object DatabaseFlowService {
  private[this] var app: Option[WebApplication] = None

  def main(args: Array[String]) {
    app = Some(new WebApplication())
    app.foreach(_.start())
  }

  def restart() = app.foreach { a =>
    a.stop()
    a.start()
  }
}
