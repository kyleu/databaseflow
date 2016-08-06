package services.socket

import models.TransactionStatus
import models.database.Transaction
import models.query.TransactionState

trait TransactionHelper { this: SocketService =>
  protected[this] var activeTransaction: Option[Transaction] = None

  private[this] def sendState(state: TransactionState) = out ! TransactionStatus(state = state, occurred = System.currentTimeMillis)

  def handleBeginTransaction() = activeTransaction match {
    case Some(tx) => throw new IllegalStateException("Already in a transaction.")
    case None =>
      val connection = db.source.getConnection
      connection.setAutoCommit(false)
      val transaction = new Transaction(connection)
      activeTransaction = Some(transaction)
      sendState(TransactionState.Started)
  }

  def handleRollbackTransaction() = activeTransaction match {
    case Some(tx) =>
      tx.rollback()
      tx.close()
      activeTransaction = None
      sendState(TransactionState.RolledBack)
    case None => throw new IllegalStateException("Not currently in a transaction.")
  }

  def handleCommitTransaction() = activeTransaction match {
    case Some(tx) =>
      tx.commit()
      tx.close()
      activeTransaction = None
      sendState(TransactionState.Committed)
    case None => throw new IllegalStateException("Not currently in a transaction.")
  }
}
