package services

import models.{BeginTransaction, CommitTransaction, RollbackTransaction, TransactionStatus}
import models.query.TransactionState
import utils.NetworkMessage

object TransactionService {
  private[this] var inTransaction = false

  def beginTransaction() = if (inTransaction) {
    throw new IllegalStateException("Attempted to begin nested transaction.")
  } else {
    NetworkMessage.sendMessage(BeginTransaction)
    utils.Logging.info("TX Start!")
  }

  def transactionStatus(status: TransactionStatus) = status.state match {
    case TransactionState.Started => inTransaction = true
    case TransactionState.NotStarted => inTransaction = false
    case TransactionState.RolledBack => inTransaction = false
    case TransactionState.Committed => inTransaction = false
  }

  def commitTransaction() = if (inTransaction) {
    NetworkMessage.sendMessage(CommitTransaction)
    utils.Logging.info("TX Commit!")
  } else {
    throw new IllegalStateException("Commit attempted while not in a transaction.")
  }

  def rollbackTransaction() = if (inTransaction) {
    NetworkMessage.sendMessage(RollbackTransaction)
    utils.Logging.info("TX Rollback!")
  } else {
    throw new IllegalStateException("Rollback attempted while not in a transaction.")
  }
}
