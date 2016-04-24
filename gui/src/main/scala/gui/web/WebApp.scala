package gui.web

trait WebApp {
  def started: Boolean
  def start()
  def stop()
}
