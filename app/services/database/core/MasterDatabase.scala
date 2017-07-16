package services.database.core

import java.util.UUID

import models.database._
import util.Logging

object MasterDatabase extends CoreDatabase with Logging {
  override val connectionId = UUID.fromString("00000000-0000-0000-0000-000000000000")
  override val name = s"${util.Config.projectName} Storage"
  override val slug = s"${util.Config.projectSlug}-storage"
  override val title = s"${util.Config.projectName} Storage"
  override val description = s"Internal storage used by ${util.Config.projectName}."
  override val configKey = "master"
  override val dbName = "databaseflow"

  def query[A](q: RawQuery[A]) = conn.query(q)
  def executeUnknown[A](q: Query[A], resultId: Option[UUID] = None) = conn.executeUnknown(q, resultId)
  def executeUpdate(s: Statement) = conn.executeUpdate(s)
  def transaction[A](f: Transaction => A) = conn.transaction(f)
}
