import services.supervisor.VersionService

object DatabaseFlowUpdated {
  private[this] def log(s: String) = println(s)

  def main(args: Array[String]): Unit = {
    log(s"Database Flow updated to version [${VersionService.localVersion}]. Please restart the application.")
  }
}
