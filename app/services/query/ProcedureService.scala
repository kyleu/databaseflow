package services.query

import java.util.UUID

import services.schema.SchemaService
import utils.Logging

object ProcedureService extends Logging {
  def callProcedure(connectionId: UUID, userId: UUID, queryId: UUID, name: String, params: Map[String, String], resultId: UUID) = {
    SchemaService.getProcedure(connectionId, name) match {
      case Some(proc) =>
        log.info(s"Calling [${proc.name}(${proc.getValues(params).map(x => x._1 + " = " + x._2).mkString(", ")})].")
        log.info(s"Connection: $connectionId, User: $userId, Query: $queryId, Result: $resultId.")
      case None => throw new IllegalStateException(s"Unknown procedure [$name].")
    }
  }
}
