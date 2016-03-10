package services.notification

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import upickle.Js
import utils.{ Config, Logging }

import scala.concurrent.Future

@javax.inject.Singleton
class NotificationService @javax.inject.Inject() (ws: WSClient, config: Config) extends Logging {
  private[this] val defaultIcon = "http://databaseflow.com/assets/images/ui/favicon/favicon.png"

  def alert(msg: String, channel: String = "#general", username: String = "Puzzle Brawl", iconUrl: String = defaultIcon) = if (config.slackEnabled) {
    val body = Js.Obj(
      "channel" -> Js.Str(channel),
      "username" -> Js.Str(username),
      "icon_url" -> Js.Str(iconUrl),
      "text" -> Js.Str(msg)
    )

    val call = ws.url(config.slackUrl).withHeaders("Accept" -> "application/json")
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
