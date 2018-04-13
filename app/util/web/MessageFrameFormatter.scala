package util.web

import models.{RequestMessage, ResponseMessage}
import play.api.mvc.WebSocket.MessageFlowTransformer
import util.Logging

import util.JsonSerializers._

class MessageFrameFormatter(debug: Boolean) extends Logging {
  val stringTransformer = MessageFlowTransformer.stringMessageFlowTransformer.map(s => decodeJson[RequestMessage](s) match {
    case Right(x) => x
    case Left(err) => throw err
  }).contramap { rm: ResponseMessage => rm.asJson.spaces2 }

  def transformer(binary: Boolean) = stringTransformer
}
