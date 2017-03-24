package services.query

import models.query.TransactionState
import models.{BeginTransaction, CommitTransaction, RollbackTransaction}
import org.scalajs.jquery.{jQuery => $}
import services.NotificationService
import utils.{NetworkMessage, NumberUtils, TemplateUtils}

import scalatags.Text.all._

object TransactionService {
  private[this] var inTransaction = false
  private[this] var numStatements = 0

  private[this] lazy val transactionPanel = $("#tx-panel")
  private[this] lazy val statusMessage = $("#tx-status-msg", transactionPanel)

  private[this] lazy val beginTransactionLink = $("#begin-tx-link")

  def isInTransaction = inTransaction

  def beginTransaction() = if (inTransaction) {
    throw new IllegalStateException("Attempted to begin nested transaction.")
  } else {
    NetworkMessage.sendMessage(BeginTransaction)
    beginTransactionLink.hide()
    transactionPanel.show()
  }

  def incrementCount() = if (inTransaction) {
    numStatements += 1
    $("#tx-statement-count", transactionPanel).text(numStatements.toString)
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

  def handleTransactionStatus(state: TransactionState, occurred: Long) = state match {
    case TransactionState.Started => onStarted(occurred)
    case TransactionState.NotStarted => onNotStarted()
    case TransactionState.RolledBack => onRollback()
    case TransactionState.Committed => onCommit()
  }

  private[this] def onStarted(occurred: Long) = {
    inTransaction = true
    val msg = div(
      "Transaction started ",
      TemplateUtils.toTimeago(TemplateUtils.toIsoString(occurred)),
      ". Completed ",
      span(id := "tx-statement-count")("0"),
      " statements."
    )
    statusMessage.html(msg.toString)
    TemplateUtils.relativeTime()
  }
  private[this] def resetCounters() = {
    inTransaction = false
    numStatements = 0
  }

  private[this] def onNotStarted() = {
    resetCounters()
  }

  private[this] def onRollback() = {
    complete("Transaction Rolled Back", s"Rolled back ${NumberUtils.withCommas(numStatements)} statements.")
    resetCounters()
  }

  private[this] def onCommit() = {
    complete("Transaction Committed Successfully", s"Persisted results of ${NumberUtils.withCommas(numStatements)} statements.")
    resetCounters()
  }

  private[this] def complete(r: String, s: String) = {
    beginTransactionLink.show()
    transactionPanel.hide()
    NotificationService.info(r, s)
    inTransaction = false
  }
}
