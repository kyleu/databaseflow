package services.sandbox

import models.settings.SettingKey
import utils.ApplicationContext

import scala.concurrent.Future

object ShowSettings extends SandboxTask {
  override def id = "settings"
  override def name = "Show Settings"
  override def description = ""

  override def run(ctx: ApplicationContext) = {
    val ret = SettingKey.values.map { k =>
      s"$k: ${ctx.settings(k)} (${ctx.settings.isOverride(k)})"
    }.mkString("\n")
    Future.successful(ret)
  }
}
