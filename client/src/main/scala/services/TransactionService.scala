package services

import models.{BeginTransaction, CommitTransaction, RollbackTransaction}
import models.query.TransactionState
import utils.{NetworkMessage, NumberUtils, TemplateUtils}
import org.scalajs.jquery.{jQuery => $}

import scalatags.Text.all._

object TransactionService {
  private[this] var inTransaction = false

  private[this] lazy val transactionPanel = $("#tx-panel")
  private[this] lazy val statusMessage = $("#tx-status-msg", transactionPanel)

  private[this] lazy val beginTransactionLink = $("#begin-tx-link")

  def beginTransaction() = if (inTransaction) {
    throw new IllegalStateException("Attempted to begin nested transaction.")
  } else {
    NetworkMessage.sendMessage(BeginTransaction)
    beginTransactionLink.hide()
    transactionPanel.show()
  }

  def commitTransaction() = if (inTransaction) {
    NetworkMessage.sendMessage(CommitTransaction)
  } else {
    throw new IllegalStateException("Commit attempted while not in a transaction.")
  }

  def rollbackTransaction() = if (inTransaction) {
    NetworkMessage.sendMessage(RollbackTransaction)
  } else {
    throw new IllegalStateException("Rollback attempted while not in a transaction.")
  }

  def handleTransactionStatus(state: TransactionState, statementCount: Int, occurred: Long) = state match {
    case TransactionState.Started => onStarted(statementCount, occurred)
    case TransactionState.NotStarted => onNotStarted()
    case TransactionState.RolledBack => onRollback(statementCount)
    case TransactionState.Committed => onCommit(statementCount)
  }

  private[this] def onStarted(statementCount: Int, occurred: Long) = {
    inTransaction = true
    val msg = div(
      "Transaction started ",
      TemplateUtils.toTimeago(TemplateUtils.toIsoString(occurred)),
      ". Completed ",
      span(NumberUtils.withCommas(statementCount)),
      " statements."
    )
    statusMessage.html(msg.toString)
    TemplateUtils.relativeTime()
  }

  private[this] def onNotStarted() = {
    inTransaction = false
  }

  private[this] def onRollback(statementCount: Int) = complete(
    "Transaction Rolled Back", s"Rolled back ${NumberUtils.withCommas(statementCount)} statements."
  )

  private[this] def onCommit(statementCount: Int) = complete(
    "Transaction Committed Successfully",
    s"Persisted results of ${NumberUtils.withCommas(statementCount)} statements."
  )

  private[this] def complete(r: String, s: String) = {
    beginTransactionLink.show()
    transactionPanel.hide()
    NotificationService.info(r, s)
    inTransaction = false
  }
}
