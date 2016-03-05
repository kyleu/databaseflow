package json

import models._
import upickle._
import upickle.legacy._

object ResponseMessageSerializers {
  private implicit val responseMessageReader: Reader[ResponseMessage] = Reader[ResponseMessage] {
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

  private implicit val responseMessageWriter: Writer[ResponseMessage] = Writer[ResponseMessage] {
    case rm =>
      val jsVal = rm match {
        case se: ServerError => writeJs(se)
        case vr: VersionResponse => writeJs(vr)
        case is: InitialState => writeJs(is)

        case p: Pong => writeJs(p)
        case SendTrace => Js.Obj()
        case dr: DebugResponse => writeJs(dr)
        case d: Disconnected => writeJs(d)

        case ms: MessageSet => writeJs(ms)
      }
      val jsArray = jsVal match { case arr: Js.Arr => arr; case _ => throw new IllegalArgumentException(jsVal.toString) }
      jsArray.value.toList match {
        case one :: two :: Nil =>
          val oneStr = Js.Str(one match {
            case s: Js.Str => s.value.replace("models.", "")
            case _ => throw new IllegalStateException()
          })
          Js.Obj("c" -> oneStr, "v" -> two)
        case _ => throw new IllegalStateException()
      }
  }

  def read(json: Js.Value) = readJs[ResponseMessage](json)
  def write(rm: ResponseMessage) = writeJs(rm)
}
