package services.database

import models.database.{Query, Queryable, Statement}
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object DatabaseWorkerPool extends Logging {
  private[this] implicit val ctx: ExecutionContext = {
    ExecutionContext.fromExecutor(new java.util.concurrent.ForkJoinPool(16))
  }

  def submitQuery[T](q: Query[T], db: Queryable, onSuccess: (T) => Unit, onFailure: (Throwable) => Unit) = {
    submit(() => db.query(q), onSuccess, onFailure)
  }

  def submitStatement(s: Statement, db: Queryable, onSuccess: (Int) => Unit, onFailure: (Throwable) => Unit) = {
    submit(() => db.executeUpdate(s), onSuccess, onFailure)
  }

  def submitUnknown[T](q: Query[T], db: Queryable, onSuccess: (Either[T, Int]) => Unit, onFailure: (Throwable) => Unit) = {
    submit(() => db.executeUnknown(q), onSuccess, onFailure)
  }

  def submitWork[T](work: () => T, onSuccess: (T) => Unit, onFailure: (Throwable) => Unit) = {
    submit(work, onSuccess, onFailure)
  }

  private[this] def submit[T](work: () => T, onSuccess: (T) => Unit, onFailure: (Throwable) => Unit) = {
    val f = Future(work())
    f.onComplete {
      case Success(x) => onSuccess(x)
      case Failure(x) => onFailure(x)
    }
    f
  }
}
