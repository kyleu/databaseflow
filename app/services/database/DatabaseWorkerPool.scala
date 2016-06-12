package services.database

import models.database.{Query, Statement}
import utils.Logging

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, Future}

object DatabaseWorkerPool extends Logging {
  private[this] implicit val ctx: ExecutionContext = {
    ExecutionContext.fromExecutor(new ForkJoinPool(16))
  }

  def submitQuery[T](q: Query[T], db: DatabaseConnection, onSuccess: (T) => Unit, onFailure: (Throwable) => Unit) = {
    submit(() => db.query(q), onSuccess, onFailure)
  }

  def submitStatement(s: Statement, db: DatabaseConnection, onSuccess: (Int) => Unit, onFailure: (Throwable) => Unit) = {
    submit(() => db.executeUpdate(s), onSuccess, onFailure)
  }

  def submitUnknown[T](q: Query[T], db: DatabaseConnection, onSuccess: (Either[T, Int]) => Unit, onFailure: (Throwable) => Unit) = {
    submit(() => db.executeUnknown(q), onSuccess, onFailure)
  }

  def submitWork[T](work: () => T, onSuccess: (T) => Unit, onFailure: (Throwable) => Unit) = {
    submit(work, onSuccess, onFailure)
  }

  private[this] def submit[T](work: () => T, onSuccess: (T) => Unit, onFailure: (Throwable) => Unit) = {
    val f = Future(work())
    f.onSuccess { case x => onSuccess(x) }
    f.onFailure { case x => onFailure(x) }
    f
  }
}
