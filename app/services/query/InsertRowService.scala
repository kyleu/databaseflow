package services.query

import java.util.UUID

import akka.actor.ActorRef
import models.InsertRowResponse
import utils.Logging

object InsertRowService extends Logging {
  def insert(connectionId: UUID, userId: UUID, name: String, params: Map[String, String], resultId: UUID, out: Option[ActorRef]) = {
    log.info(s"Inserting row [${params.mkString(", ")}] in table [$name].")
    log.info(s"Connection: $connectionId, User: $userId, Result: $resultId.")
    val response = InsertRowResponse(resultId, Map("actor_id" -> "Invalid plumbus."))
    out.foreach(_ ! response)
  }
}
