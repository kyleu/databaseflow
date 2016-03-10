package utils.web

import models.{ MalformedRequest, RequestMessage, ResponseMessage }
import play.api.mvc.WebSocket.FrameFormatter
import upickle.{ Js, json }
import upickle.legacy._

import scala.util.control.NonFatal

import utils.json.JsonSerializers._

class MessageFrameFormatter(debug: Boolean) {
  private[this] def requestToJsValue(r: RequestMessage): Js.Value = {
    throw new IllegalArgumentException(s"Attempted to serialize RequestMessage [$r] on server.")
  }

  private[this] def requestFromJsValue(json: Js.Value): RequestMessage = try {
    readJs[RequestMessage](json)
  } catch {
    case NonFatal(x) => MalformedRequest(s"Invalid Request [${x.getClass.getSimpleName}]", json.toString)
  }

  private[this] def responseToJsValue(r: ResponseMessage): Js.Value = r match {
    //case ms: MessageSet => writeJs(ms)
    case _ => writeJs(r)
  }

  private[this] def responseFromJsValue(json: Js.Value): ResponseMessage = {
    throw new IllegalArgumentException(s"Attempted to deserialize ResponseMessage [$json] on server.")
  }

  private[this] def jsValueToString(v: Js.Value) = if(debug) { json.write(v, indent = 2) } else { json.write(v) }

  private[this] def jsValueFromString(s: String) = try {
    json.read(s)
  } catch {
    case NonFatal(x) => Js.Arr(Js.Str("models.MalformedRequest"), Js.Obj(
      "reason" -> Js.Str("Invalid JSON"),
      "content" -> Js.Str(s)
    ))
  }

  private[this] val jsValueFrame: FrameFormatter[Js.Value] = FrameFormatter.stringFrame.transform(jsValueToString, jsValueFromString)

  implicit val requestFormatter = jsValueFrame.transform(requestToJsValue, requestFromJsValue)
  implicit val responseFormatter = jsValueFrame.transform(responseToJsValue, responseFromJsValue)
}
