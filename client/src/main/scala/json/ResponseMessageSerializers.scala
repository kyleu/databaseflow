package json

import models._
import upickle._
import upickle.legacy._

object ResponseMessageSerializers {
  private[this] implicit val responseMessageReader: Reader[ResponseMessage] = Reader[ResponseMessage] {
    case json: Js.Obj =>
      val c = json.value.find(_._1 == "c").getOrElse(throw new IllegalStateException())._2 match {
        case Js.Str(s) => s
        case _ => throw new IllegalStateException()
      }
      val v = json.value.find(_._1 == "v").getOrElse(throw new IllegalStateException())._2 match {
        case o: Js.Obj => o
        case _ => throw new IllegalStateException()
      }
      val ret: ResponseMessage = json.value.find(_._1 == "v").getOrElse(throw new IllegalStateException())._2 match {
        case o: Js.Obj => c match {
          case "ServerError" => readJs[ServerError](o)
          case "VersionResponse" => readJs[VersionResponse](o)
          case "InitialState" => readJs[InitialState](o)

          case "Pong" => readJs[Pong](o)
          case "SendTrace" => SendTrace
          case "DebugResponse" => readJs[DebugResponse](o)
          case "Disconnected" => readJs[Disconnected](o)

          case "MessageSet" => readJs[MessageSet](o)
          case _ => throw new IllegalStateException()
        }
        case _ => throw new IllegalStateException()
      }
      ret
  }

  def read(json: Js.Value) = readJs[ResponseMessage](json)
}
