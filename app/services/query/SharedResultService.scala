package services.query

import java.util.UUID

import akka.actor.ActorRef
import models.SharedResultResponse
import models.queries.result.SharedResultQueries
import models.query.SharedResult
import models.user.{Role, User}
import services.database.DatabaseWorkerPool
import services.database.core.MasterDatabase
import services.user.UserService
import utils.ExceptionUtils

object SharedResultService {
  def canView(user: User, sr: SharedResult) = Role.matchPermissions(user, sr.owner, "shared result", "read", sr.viewableBy)

  def getVisible(userId: UUID) = {
    val sqq = SharedResultQueries.getVisible(userId)
    MasterDatabase.conn.query(sqq)
  }

  def getForUser(user: User, connectionId: UUID, out: ActorRef) = {
    val startMs = System.currentTimeMillis
    val sqq = SharedResultQueries.getForUser(user.id, connectionId)
    def onSharedResultsSuccess(sharedResults: Seq[SharedResult]) {
      val viewable = sharedResults.filter(sr => canView(user, sr)._1)
      val elapsedMs = (System.currentTimeMillis - startMs).toInt
      val usernameMap = viewable.map(_.owner).flatMap(uuid => UserService.instance.flatMap { inst =>
        UserService.instance.flatMap(inst => inst.usernameLookup(uuid).map(uuid -> _))
      }).toMap
      out ! SharedResultResponse(viewable, usernameMap, elapsedMs)
    }
    def onSharedResultsFailure(t: Throwable) { ExceptionUtils.actorErrorFunction(out, "SharedResultLoadException", t) }
    DatabaseWorkerPool.submitQuery(sqq, MasterDatabase.conn, onSharedResultsSuccess, onSharedResultsFailure)
  }

  def save(sr: SharedResult, userId: UUID) = MasterDatabase.query(SharedResultQueries.getById(sr.id)) match {
    case Some(existing) =>
      if (existing.owner == userId) {
        val updated = sr.copy(
          lastAccessed = System.currentTimeMillis
        )
        MasterDatabase.executeUpdate(SharedResultQueries.UpdateSharedResult(updated))
        updated
      } else {
        throw new IllegalStateException("Not Authorized.")
      }
    case None =>
      val inserted = sr.copy(owner = userId, lastAccessed = System.currentTimeMillis, created = System.currentTimeMillis)
      MasterDatabase.executeUpdate(SharedResultQueries.insert(inserted))
      inserted
  }

  def delete(id: UUID, userId: UUID) = MasterDatabase.query(SharedResultQueries.getById(id)) match {
    case Some(existing) => if (existing.owner == userId) {
      MasterDatabase.executeUpdate(SharedResultQueries.removeById(id))
    } else {
      throw new IllegalStateException(s"Attempt by [$userId] to remove shared result [$id], which is owned by [${existing.owner}].")
    }
    case None => throw new IllegalStateException(s"Unknown shared result [$id].")
  }

  private[this] def padUuid(s: String) = {
    s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16) + "-" + s.substring(16, 20) + "-" + s.substring(20, 32)
  }

  def getSharedCachedResultIds = MasterDatabase.query(SharedResultQueries.GetCachedTableNames).map { name =>
    UUID.fromString(padUuid(name.stripPrefix("result_")))
  }

  def containsResultId(resultId: UUID) = MasterDatabase.query(SharedResultQueries.ContainsCachedTableName(resultId.toString.replaceAllLiterally("-", "")))
}
