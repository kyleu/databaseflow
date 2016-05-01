package models.database

import java.sql.{ Connection, Savepoint }
import scala.collection.mutable.ListBuffer

class Transaction(connection: Connection) extends Queryable {
  private[this] var rolledback = false

  override def apply[A](query: RawQuery[A]): A = apply(connection, query)

  def executeUnknown[A](query: Query[A]): Either[A, Int] = executeUnknown(connection, query)

  def executeUpdate(statement: Statement) = executeUpdate(connection, statement)

  def rollback() {
    log.debug("Rolling back transaction")
    connection.rollback()
    rolledback = true
    onRollback.foreach(_())
  }

  def rollback(savepoint: Savepoint) {
    log.debug("Rolling back to savepoint")
    connection.rollback(savepoint)
  }

  def release(savepoint: Savepoint) {
    log.debug("Releasing savepoint")
    connection.rollback(savepoint)
  }

  def savepoint(): Savepoint = {
    log.debug("Setting unnamed savepoint")
    connection.setSavepoint()
  }

  def savepoint(name: String): Savepoint = {
    log.debug(s"Setting savepoint with name [$name].")
    connection.setSavepoint(name)
  }

  def commit() = if (!rolledback) {
    log.debug("Committing transaction")
    connection.commit()
    onCommit.foreach(_())
  }

  def close() {
    log.debug("Closing transaction")
    connection.close()
    onClose.foreach(_())
  }

  def transaction[A](f: Transaction => A): A = f(this)

  var onCommit: ListBuffer[() => Unit] = ListBuffer.empty[() => Unit]
  var onClose: ListBuffer[() => Unit] = ListBuffer.empty[() => Unit]
  var onRollback: ListBuffer[() => Unit] = ListBuffer.empty[() => Unit]
}
