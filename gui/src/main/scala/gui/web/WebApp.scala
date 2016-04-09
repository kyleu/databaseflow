package gui.web

trait WebApp {
  def started: Boolean
  def start(): Unit
  def stop(): Unit
}
