package services.database.transaction

import models.database.Transaction

class TransactionManager extends TransactionProvider {
  case class TransactionState(var transactions: List[Transaction])

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
      localTransactionStorage.set(Some(TransactionState(List(transaction))))
    } else {
      currentTransactionState.transactions = currentTransactionState.transactions :+ transaction
    }
  }

  override def end() = if (!transactionExists) {
    throw new Exception("No transaction in current context")
  } else {
    currentTransactionState.transactions = currentTransactionState.transactions.dropRight(1)
    if (currentTransactionState.transactions.isEmpty) {
      localTransactionStorage.set(None)
    }
  }

  override def rollback() = currentTransaction.rollback()
}
