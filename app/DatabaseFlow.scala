import utils.web.WebApplication

object DatabaseFlow {
  private[this] var app: Option[WebApplication] = None

  def main(args: Array[String]) {
    if (args.headOption.contains("-v")) {
      throw new IllegalArgumentException("Invalid argument.")
    }
    app = Some(new WebApplication())
    app.foreach(_.start())
  }

  def restart() = app.foreach { a =>
    a.stop()
    a.start()
  }
}
