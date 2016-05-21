package services.sandbox

import models.settings.SettingKey
import services.settings.SettingsService
import utils.ApplicationContext

import scala.concurrent.Future

object ShowSettings extends SandboxTask {
  override def id = "settings"
  override def name = "Show Settings"
  override def description = ""
  override def isHtml = true

  override def run(ctx: ApplicationContext) = {
    val ret = SettingKey.values.map { k =>
      s"<div><strong>$k</strong>: ${SettingsService(k)} (${SettingsService.isOverride(k)})</div>"
    }.mkString("\n")
    Future.successful(ret)
  }
}
