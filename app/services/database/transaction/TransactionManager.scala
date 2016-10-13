package services.database.transaction

import models.database.Transaction

import scala.collection.mutable

class TransactionManager extends TransactionProvider {
  case class TransactionState(transactions: mutable.Stack[Transaction])

  private val localTransactionStorage = new ThreadLocal[Option[TransactionState]] {
    override def initialValue = None
  }

  protected def ambientTransactionState = localTransactionStorage.get

  protected def ambientTransaction = ambientTransactionState.flatMap(t => Some(t.transactions.headOption.getOrElse {
    throw new IllegalStateException("Missing transactions.")
  }))

  protected def currentTransactionState = ambientTransactionState.getOrElse(throw new Exception("No transaction in current context"))

  def currentTransaction = ambientTransaction.getOrElse(throw new Exception("No transaction in current context"))

  override def transactionExists = ambientTransactionState.isDefined

  override def begin(transaction: Transaction) = {
    if (!transactionExists) {
      localTransactionStorage.set(Some(TransactionState(mutable.Stack(transaction))))
    } else {
      currentTransactionState.transactions.push(transaction)
    }
  }

  override def end() = if (!transactionExists) {
    throw new Exception("No transaction in current context")
  } else {
    currentTransactionState.transactions.pop
    if (currentTransactionState.transactions.isEmpty) {
      localTransactionStorage.set(None)
    }
  }

  override def rollback() = currentTransaction.rollback()
}
