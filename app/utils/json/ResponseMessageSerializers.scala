package utils.json

import models._
import models.user.UserPreferences
import play.api.libs.json._

object ResponseMessageSerializers {
  private[this] val serverErrorWrites = Json.writes[ServerError]
  private[this] val versionResponseWrites = Json.writes[VersionResponse]

  private[this] implicit val userPreferencesWrites = Json.writes[UserPreferences]
  private[this] val initialStateWrites = Json.writes[InitialState]

  private[this] val pongWrites = Json.writes[Pong]
  private[this] val debugResponseWrites = Json.writes[DebugResponse]
  private[this] val disconnectedWrites = Json.writes[Disconnected]

  implicit val responseMessageWrites = Writes[ResponseMessage] { r: ResponseMessage =>
    val json = r match {
      case se: ServerError => serverErrorWrites.writes(se)
      case p: Pong => pongWrites.writes(p)
      case vr: VersionResponse => versionResponseWrites.writes(vr)

      case is: InitialState => initialStateWrites.writes(is)

      case SendTrace => JsObject(Nil)
      case dr: DebugResponse => debugResponseWrites.writes(dr)
      case d: Disconnected => disconnectedWrites.writes(d)

      case ms: MessageSet => throw new IllegalArgumentException()
    }
    JsObject(Seq("c" -> JsString(utils.Formatter.className(r)), "v" -> json))
  }

  val messageSetWrites = Writes[MessageSet] { ms: MessageSet =>
    JsObject(Seq("c" -> JsString("MessageSet"), "v" -> JsObject(Seq("messages" -> JsArray(ms.messages.map(responseMessageWrites.writes))))))
  }
}
