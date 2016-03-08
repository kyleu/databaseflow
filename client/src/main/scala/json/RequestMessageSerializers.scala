package json

import models._
import upickle._
import upickle.legacy._

object RequestMessageSerializers {
  private[this] implicit val requestMessageWriter: Writer[RequestMessage] = Writer[RequestMessage] {
    case rm =>
      val jsVal = rm match {
        case mf: MalformedRequest => writeJs(mf)
        case p: Ping => writeJs(p)
        case GetVersion => Js.Obj
        case dr: DebugInfo => writeJs(dr)
        case sq: SubmitQuery => writeJs(sq)
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

  def write(rm: RequestMessage) = writeJs(rm)
}
