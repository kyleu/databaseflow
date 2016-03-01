package models.database

import java.sql.{Connection, PreparedStatement, Types}

import utils.{Logging, NullUtils}

import scala.annotation.tailrec

trait Queryable extends Logging {
  private[this] def prependComment(obj: Object, sql: String) = s"/* ${obj.getClass.getSimpleName.replace("$", "")} */ $sql"

  @tailrec
  private[this] def prepare(stmt: PreparedStatement, values: Seq[Any], index: Int = 1) {
    if (values.nonEmpty) {
      values.head match {
        case v if NullUtils.isNull(v) => stmt.setNull(index, Types.NULL)

        case ov: Option[_] if ov.isDefined => stmt.setObject(index, Conversions.convert(ov.get.asInstanceOf[AnyRef]))
        case ov: Option[_] if ov.isEmpty => stmt.setNull(index, Types.NULL)

        case v => stmt.setObject(index, Conversions.convert(v.asInstanceOf[AnyRef]))
      }
      prepare(stmt, values.tail, index + 1)
    }
  }

  def apply[A](connection: Connection, query: RawQuery[A]): A = {
    log.debug(s"${query.sql} with ${query.values.mkString("(", ", ", ")")}")
    val stmt = connection.prepareStatement(prependComment(query, query.sql))
    try {
      prepare(stmt, query.values)
      val results = stmt.executeQuery()
      try {
        query.handle(results)
      } finally {
        results.close()
      }
    } finally {
      stmt.close()
    }
  }

  def execute(connection: Connection, statement: Statement): Int = {
    log.debug(s"${statement.sql} with ${statement.values.mkString("(", ", ", ")")}")
    val stmt = connection.prepareStatement(prependComment(statement, statement.sql))
    try {
      prepare(stmt, statement.values)
      stmt.executeUpdate()
    } finally {
      stmt.close()
    }
  }

  def execute(statement: Statement): Int
  def apply[A](query: RawQuery[A]): A
  def transaction[A](f: Transaction => A): A

  def query[A](query: RawQuery[A]): A = apply(query)

  def update(statement: Statement): Int = execute(statement)
  def insert(statement: Statement): Int = execute(statement)
  def delete(statement: Statement): Int = execute(statement)
}
