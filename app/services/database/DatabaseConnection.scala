package services.database

import java.sql.Connection
import java.util.UUID
import javax.sql.DataSource

import com.codahale.metrics.MetricRegistry
import com.zaxxer.hikari.HikariDataSource
import models.database._
import models.engine.DatabaseEngine
import services.database.transaction.{TransactionManager, TransactionProvider}
import utils.metrics.Instrumented

import scala.util.control.NonFatal

case class DatabaseConnection(connectionId: UUID, name: String, source: DataSource, engine: DatabaseEngine, url: String, username: String) extends Queryable {
  private[this] def time[A](klass: java.lang.Class[_])(f: => A) = {
    val ctx = Instrumented.metricRegistry.timer(MetricRegistry.name(klass)).time()
    try { f } finally { ctx.stop }
  }

  val transactionProvider: TransactionProvider = new TransactionManager

  def transaction[A](f: Transaction => A): A = transaction(logError = true, forceNew = false, f)

  def transaction[A](logError: Boolean, forceNew: Boolean, f: Transaction => A): A = {
    if (!forceNew && transactionProvider.transactionExists) {
      f(transactionProvider.currentTransaction)
    } else {
      val connection = source.getConnection
      connection.setAutoCommit(false)
      val txn = new Transaction(connection)
      try {
        log.debug("Starting transaction")
        val result = f(txn)
        txn.commit()
        result
      } catch {
        case NonFatal(t) =>
          if (logError) { log.error("Exception thrown in transaction scope; aborting transaction", t) }
          txn.rollback()
          throw t
      } finally {
        txn.close()
      }
    }
  }

  def apply[A](query: RawQuery[A]): A = if (transactionProvider.transactionExists) {
    transactionProvider.currentTransaction(query)
  } else {
    val connection = source.getConnection
    try { time(query.getClass) { apply(connection, query) } } finally { connection.close() }
  }

  def executeUnknown[A](query: Query[A], resultId: Option[UUID] = None): Either[A, Int] = if (transactionProvider.transactionExists) {
    transactionProvider.currentTransaction.executeUnknown(query, resultId)
  } else {
    val connection = source.getConnection
    try { time(query.getClass) { executeUnknown(connection, query, resultId) } } finally { connection.close() }
  }

  def executeUpdate(statement: Statement) = {
    if (transactionProvider.transactionExists) {
      transactionProvider.currentTransaction.executeUpdate(statement)
    } else {
      val connection = source.getConnection
      try { time(statement.getClass) { executeUpdate(connection, statement) } } finally { connection.close() }
    }
  }

  def close() = if (source.isWrapperFor(classOf[HikariDataSource])) {
    source.unwrap(classOf[HikariDataSource]).close()
  }

  def withConnection[T](f: (Connection) => T) = {
    val conn = source.getConnection()
    try { f(conn) } finally { conn.close() }
  }
}
