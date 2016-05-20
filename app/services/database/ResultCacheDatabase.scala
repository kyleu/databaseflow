package services.database

import utils.Logging

object ResultCacheDatabase extends Logging {
  private[this] var connOpt: Option[DatabaseConnection] = None

  def open() = {
    connOpt.foreach(x => throw new IllegalStateException("Result Cache database already open."))

    connOpt = Some(MasterDatabase.conn)
  }

  def conn = connOpt.getOrElse(throw new IllegalStateException("Result Cache database connection not open."))

  def close() = {
    connOpt.foreach(_.close())
    connOpt = None
    log.info("Result Cache database closed.")
  }
}
