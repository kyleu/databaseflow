package services.socket

import java.util.UUID

import models.TransactionStatus
import models.database.Transaction
import models.query.TransactionState

trait TransactionHelper { this: SocketService =>
  private[this] val activeTransactions = collection.mutable.HashMap.empty[UUID, (Transaction, Int)]

  def handleBeginTransaction(queryId: UUID) = {
    out ! TransactionStatus(queryId = queryId, state = TransactionState.Started, 0)
  }

  def handleRollbackTransaction(queryId: UUID) {
    out ! TransactionStatus(queryId = queryId, state = TransactionState.RolledBack, 0)
  }

  def handleCommitTransaction(queryId: UUID) = {
    out ! TransactionStatus(queryId = queryId, state = TransactionState.Committed, 0)
  }
}
