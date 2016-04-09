package gui.utils

object InitialProperties {
  def set() = {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Database Flow")
  }
}
