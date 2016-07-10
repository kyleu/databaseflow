package services.notification

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import upickle.Js
import utils.Logging

import scala.concurrent.Future

@javax.inject.Singleton
class SlackService @javax.inject.Inject() (ws: WSClient) extends Logging {
  private[this] val enabled = true
  private[this] val url = "https://hooks.slack.com/services/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"
  private[this] val defaultIcon = "http://databaseflow.com/assets/images/ui/favicon/favicon.png"

  def alert(msg: String, channel: String = "#general", username: String = "Database Flow", iconUrl: String = defaultIcon) = if (enabled) {
    val body = Js.Obj(
      "channel" -> Js.Str(channel),
      "username" -> Js.Str(username),
      "icon_url" -> Js.Str(iconUrl),
      "text" -> Js.Str(msg)
    )

    val call = ws.url(url).withHeaders("Accept" -> "application/json")
    val f = call.post(upickle.json.write(body))
    val ret = f.map { x =>
      if (x.status == 200) {
        "OK"
      } else {
        log.warn("Unable to post to Slack (" + x.status + "): [" + x.body + "].")
        "ERROR"
      }
    }
    ret.onFailure {
      case x => log.warn("Unable to post to Slack.", x)
    }
    ret
  } else {
    log.info(s"Post to [$channel] with Slack disabled: [$msg].")
    Future.successful("Slack is not enabled.")
  }
}
