package services

import models.{BeginTransaction, CommitTransaction, RollbackTransaction, TransactionStatus}
import models.query.TransactionState
import utils.NetworkMessage
import org.scalajs.jquery.{jQuery => $}

object TransactionService {
  private[this] var inTransaction = false

  private[this] lazy val transactionPanel = $("#tx-panel")
  private[this] lazy val statusMessage = $("#tx-status-msg", transactionPanel)
  private[this] lazy val commitButton = $("#commit-button", transactionPanel)
  private[this] lazy val rollbackButton = $("#rollback-button", transactionPanel)

  private[this] lazy val beginTransactionLink = $("#begin-tx-link")

  def beginTransaction() = if (inTransaction) {
    throw new IllegalStateException("Attempted to begin nested transaction.")
  } else {
    NetworkMessage.sendMessage(BeginTransaction)
    beginTransactionLink.hide()
    transactionPanel.show()
  }

  def transactionStatus(status: TransactionStatus) = status.state match {
    case TransactionState.Started =>
      inTransaction = true
      transactionPanel.text("Stuff!")
    case TransactionState.NotStarted =>
      inTransaction = false
    case TransactionState.RolledBack =>
      inTransaction = false
    case TransactionState.Committed =>
      inTransaction = false
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
