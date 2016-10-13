package services.database.transaction

import models.database.Transaction

trait TransactionProvider {
  def transactionExists: Boolean
  def currentTransaction: Transaction
  def begin(transaction: Transaction): Unit
  def end(): Unit
  def rollback(): Unit
}
